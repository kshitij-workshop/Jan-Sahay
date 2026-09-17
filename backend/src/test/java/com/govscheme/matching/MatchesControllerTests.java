package com.govscheme.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.auth.TestMailConfig;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeRepository;
import com.govscheme.matching.entity.UserSchemeMatchRepository;
import com.govscheme.eligibility.entity.SchemeEligibilityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class MatchesControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private UserSchemeMatchRepository matchRepository;

    @Autowired
    private SchemeEligibilityRepository eligibilityRepository;

    @org.junit.jupiter.api.BeforeEach
    void cleanCatalog() {
        matchRepository.deleteAll();
        eligibilityRepository.deleteAll();
        schemeRepository.deleteAll();
    }

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@example.com";
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email, "phone", "9" + UUID.randomUUID().toString().replace("-", "").substring(0, 9),
                    "password", "Password123", "fullName", "Test User"))))
            .andExpect(status().isOk());
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("identifier", email, "password", "Password123"))))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode json = objectMapper.readTree(login.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    @Test
    void matchesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/matches")).andExpect(status().isForbidden());
    }

    @Test
    void profileWriteProducesMatchesAndIsolationHolds() throws Exception {
        String emailA = uniqueEmail("a");
        String tokenA = registerAndLogin(emailA);
        String emailB = uniqueEmail("b");
        String tokenB = registerAndLogin(emailB);

        Scheme scheme = new Scheme();
        scheme.setSlug("matchable-" + UUID.randomUUID());
        scheme.setSchemeName("Matchable");
        scheme.setSource("TEST");
        schemeRepository.save(scheme);

        // A completes their profile; the write triggers recalculation.
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("dateOfBirth", "2004-08-15");
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk());

        // A sees exactly their own row.
        mockMvc.perform(get("/api/matches")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].schemeId").value(scheme.getId()));

        // B has no row: the same scheme id must 404 for them.
        mockMvc.perform(get("/api/matches/" + scheme.getId())
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isNotFound());

        // B writes too and gets their own row; neither sees the other's.
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("fullName", "User B"))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/matches")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].schemeId").value(scheme.getId()));

        mockMvc.perform(get("/api/matches")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/matches/" + scheme.getId())
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.schemeName").value("Matchable"));

        mockMvc.perform(get("/api/matches/" + scheme.getId())
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.schemeName").value("Matchable"));
    }

    @Test
    void statusFilterNarrowsResults() throws Exception {
        String token = registerAndLogin(uniqueEmail("filter"));

        Scheme scheme = new Scheme();
        scheme.setSlug("filterable-" + UUID.randomUUID());
        scheme.setSchemeName("Filterable");
        scheme.setSource("TEST");
        schemeRepository.save(scheme);

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("fullName", "Filter User"))))
            .andExpect(status().isOk());

        // Unconstrained scheme with no disqualifiers: ELIGIBLE (reason states
        // no machine-readable constraints were published).
        mockMvc.perform(get("/api/matches")
                .param("status", "ELIGIBLE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/matches")
                .param("status", "NOT_ELIGIBLE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }
}
