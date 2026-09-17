package com.govscheme.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.auth.TestMailConfig;
import com.govscheme.profile.entity.UserProfileRepository;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
class ProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository profileRepository;

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@example.com";
    }

    private String registerAndLogin(String email, String phone) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email, "phone", phone, "password", "Password123", "fullName", "Test User"))))
            .andExpect(status().isOk());
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("identifier", email, "password", "Password123"))))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode json = objectMapper.readTree(login.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    private String userIdOf(String email) throws Exception {
        MvcResult me = mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + tokenFor(email)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(me.getResponse().getContentAsString()).get("data").get("id").asText();
    }

    private String tokenFor(String email) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("identifier", email, "password", "Password123"))))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
            .get("data").get("accessToken").asText();
    }

    @Test
    void anonymousRequestsAreDenied() throws Exception {
        mockMvc.perform(get("/api/profile")).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAutoCreatesEmptyProfile() throws Exception {
        String email = uniqueEmail("profile");
        String token = registerAndLogin(email, "9100000001");

        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.completeness.overallPercent").value(0))
            .andExpect(jsonPath("$.data.nationality").value("Indian"))
            .andExpect(jsonPath("$.data.addresses").isArray());

        assertThat(profileRepository.count()).isEqualTo(1);
    }

    @Test
    void putSavesFullProfileAndComputesCompleteness() throws Exception {
        String email = uniqueEmail("full");
        String token = registerAndLogin(email, "9100000002");

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fullBody())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fullName").value("Rahul Kumar"))
            .andExpect(jsonPath("$.data.completeness.overallPercent").value(100))
            .andExpect(jsonPath("$.data.addresses.length()").value(1))
            .andExpect(jsonPath("$.data.addresses[0].district").value("Sheikhpura"));

        // Persisted: a fresh GET returns the same data.
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.annualIncome").value(150000))
            .andExpect(jsonPath("$.data.completeness.overallPercent").value(100));
    }

    @Test
    void putMergesPartialUpdates() throws Exception {
        String email = uniqueEmail("merge");
        String token = registerAndLogin(email, "9100000003");

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("fullName", "Rahul Kumar"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fullName").value("Rahul Kumar"));

        // Second update touches a different field; the first must survive.
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("annualIncome", 150000))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fullName").value("Rahul Kumar"))
            .andExpect(jsonPath("$.data.annualIncome").value(150000));
    }

    @Test
    void putRejectsMalformedValuesButReportsMissingAsCompleteness() throws Exception {
        String email = uniqueEmail("invalid");
        String token = registerAndLogin(email, "9100000004");

        // Malformed values -> 400 with field details.
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "dateOfBirth", "2999-01-01",
                    "annualIncome", -5,
                    "addresses", List.of(Map.of("pincode", "123"))))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.dateOfBirth").exists())
            .andExpect(jsonPath("$.details.annualIncome").exists());

        // Merely absent values -> 200 with missingFields, never 400.
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "disabilityStatus", true))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.completeness.missingFields").isArray())
            .andExpect(jsonPath("$.data.completeness.missingFields[?(@=='social.disabilityPercentage')]").exists());
    }

    @Test
    void usersCannotSeeEachOthersProfiles() throws Exception {
        String emailA = uniqueEmail("userA");
        String tokenA = registerAndLogin(emailA, "9100000005");
        String emailB = uniqueEmail("userB");
        String tokenB = registerAndLogin(emailB, "9100000006");

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("fullName", "User A"))))
            .andExpect(status().isOk());

        // B's profile is untouched by A's write.
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.completeness.overallPercent").value(0))
            .andExpect(jsonPath("$.data.completeness.missingFields[?(@=='personal.fullName')]").exists());

        assertThat(userIdOf(emailA)).isNotEqualTo(userIdOf(emailB));
    }

    @Test
    void addressesReplaceWhenProvidedAndSurviveWhenAbsent() throws Exception {
        String email = uniqueEmail("addr");
        String token = registerAndLogin(email, "9100000007");

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("addresses", List.of(
                    Map.of("state", "Bihar", "district", "Patna", "villageTown", "Patna",
                        "pincode", "800001", "primary", true),
                    Map.of("state", "Bihar", "district", "Gaya", "villageTown", "Gaya",
                        "pincode", "823001", "primary", true))))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.addresses.length()").value(2));

        // Exactly one primary survives even when two were flagged.
        MvcResult afterFlags = mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode addresses = objectMapper.readTree(afterFlags.getResponse().getContentAsString())
            .get("data").get("addresses");
        long primaries = 0;
        for (JsonNode a : addresses) {
            if (a.get("primary").asBoolean()) {
                primaries++;
            }
        }
        assertThat(primaries).isEqualTo(1);

        // Scalar-only update preserves the address set.
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("fullName", "Rahul Kumar"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.addresses.length()").value(2));
    }

    private Map<String, Object> fullBody() {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("fullName", "Rahul Kumar");
        body.put("dateOfBirth", "2004-08-15");
        body.put("gender", "MALE");
        body.put("educationLevel", "GRADUATE");
        body.put("studentStatus", false);
        body.put("annualIncome", 150000);
        body.put("incomeCategory", "APL");
        body.put("casteCategory", "OBC");
        body.put("disabilityStatus", false);
        body.put("occupation", "FARMER");
        body.put("employmentStatus", "SELF_EMPLOYED");
        body.put("maritalStatus", "UNMARRIED");
        body.put("addresses", List.of(Map.of(
            "state", "Bihar", "district", "Sheikhpura", "villageTown", "Sheikhpura",
            "pincode", "811105", "areaType", "RURAL", "primary", true)));
        return body;
    }
}
