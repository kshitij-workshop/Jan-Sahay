package com.govscheme.eligibility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.auth.TestMailConfig;
import com.govscheme.eligibility.entity.SchemeEligibility;
import com.govscheme.eligibility.entity.SchemeEligibilityRepository;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
class EligibilityControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeEligibilityRepository eligibilityRepository;

    private String schemeId;

    @BeforeEach
    void seed() {
        eligibilityRepository.deleteAll();
        schemeRepository.deleteAll();

        Scheme scheme = new Scheme();
        scheme.setSlug("test-scholarship");
        scheme.setSchemeName("Test Scholarship");
        scheme.setSource("TEST");
        schemeRepository.save(scheme);
        schemeId = scheme.getId();

        SchemeEligibility criteria = new SchemeEligibility();
        criteria.setSchemeId(schemeId);
        criteria.setStates("Bihar");
        criteria.setRequireStudent(true);
        criteria.setMaxAnnualIncome(200000L);
        eligibilityRepository.save(criteria);
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
    void eligibilityRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/schemes/" + schemeId + "/eligibility"))
            .andExpect(status().isForbidden());
    }

    @Test
    void unknownSchemeReturns404() throws Exception {
        String token = registerAndLogin(uniqueEmail("elig"));

        mockMvc.perform(get("/api/schemes/does-not-exist/eligibility")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    void emptyProfileYieldsInsufficientInformation() throws Exception {
        String token = registerAndLogin(uniqueEmail("empty"));

        mockMvc.perform(get("/api/schemes/" + schemeId + "/eligibility")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("INSUFFICIENT_INFORMATION"))
            .andExpect(jsonPath("$.data.missingFields").isArray())
            .andExpect(jsonPath("$.data.rules").isArray());
    }

    @Test
    void matchingProfileYieldsEligibleWithExplanations() throws Exception {
        String token = registerAndLogin(uniqueEmail("match"));

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("dateOfBirth", "2004-08-15");
        body.put("gender", "MALE");
        body.put("studentStatus", true);
        body.put("annualIncome", 150000);
        body.put("addresses", java.util.List.of(Map.of(
            "state", "Bihar", "district", "Patna", "villageTown", "Patna",
            "pincode", "800001", "primary", true)));
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/schemes/" + schemeId + "/eligibility")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ELIGIBLE"))
            .andExpect(jsonPath("$.data.rules[?(@.rule=='STATE')].status").value("PASS"))
            .andExpect(jsonPath("$.data.rules[?(@.rule=='STUDENT')].status").value("PASS"))
            .andExpect(jsonPath("$.data.rules[?(@.rule=='INCOME')].status").value("PASS"));
    }

    @Test
    void mismatchedProfileYieldsNotEligible() throws Exception {
        String token = registerAndLogin(uniqueEmail("mismatch"));

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("studentStatus", true);
        body.put("annualIncome", 150000);
        body.put("addresses", java.util.List.of(Map.of(
            "state", "Kerala", "district", "Kochi", "villageTown", "Kochi",
            "pincode", "682001", "primary", true)));
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/schemes/" + schemeId + "/eligibility")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("NOT_ELIGIBLE"));
    }
}
