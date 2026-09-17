package com.govscheme.scheme.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.auth.TestMailConfig;
import com.govscheme.auth.entity.User;
import com.govscheme.auth.entity.UserRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.import.file=src/test/resources/scheme-import-fixture.json")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class SchemeImportAdminTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SchemeRepository schemeRepository;

    @BeforeEach
    void clean() {
        schemeRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@example.com";
    }

    private String login(String identifier) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("identifier", identifier, "password", "Password123"))))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    private String registerUser(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email, "phone", "9" + UUID.randomUUID().toString().replace("-", "").substring(0, 9),
                    "password", "Password123", "fullName", "Test User"))))
            .andExpect(status().isOk());
        return login(email);
    }

    private String createAdmin() {
        User admin = new User();
        admin.setEmail(uniqueEmail("admin"));
        admin.setPhone("8" + UUID.randomUUID().toString().replace("-", "").substring(0, 9));
        admin.setFullName("Admin User");
        admin.setPasswordHash(passwordEncoder.encode("Password123"));
        admin.setRole(User.Role.ADMIN);
        admin.setIsActive(true);
        admin.setEmailVerified(true);
        admin.setPhoneVerified(false);
        userRepository.save(admin);
        return admin.getEmail();
    }

    @Test
    void importRequiresAdmin() throws Exception {
        String userToken = registerUser(uniqueEmail("citizen"));

        mockMvc.perform(post("/api/admin/schemes/import"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/schemes/import")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanTriggerFileImport() throws Exception {
        String adminToken = login(createAdmin());

        mockMvc.perform(post("/api/admin/schemes/import")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.fetched").value(1))
            .andExpect(jsonPath("$.data.createdCount").value(1));

        assertThatSchemeImported();
    }

    private void assertThatSchemeImported() {
        var scheme = schemeRepository.findBySlug("fixture-scheme").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(scheme.getBenefitsMd()).isNull();
        org.assertj.core.api.Assertions.assertThat(scheme.getEligibilityMd()).isEqualTo("Eligibility");
        org.assertj.core.api.Assertions.assertThat(scheme.getSource()).isEqualTo("FILE");
    }
}
