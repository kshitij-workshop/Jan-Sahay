package com.govscheme.scheme.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.auth.TestMailConfig;
import com.govscheme.auth.entity.User;
import com.govscheme.auth.entity.UserRepository;
import com.govscheme.profile.entity.UserAddressRepository;
import com.govscheme.profile.entity.UserProfileRepository;
import com.govscheme.scheme.client.MySchemeClient;
import com.govscheme.scheme.client.StubMySchemeClient;
import com.govscheme.scheme.entity.SchemeApplicationStepRepository;
import com.govscheme.scheme.entity.SchemeDocumentRepository;
import com.govscheme.scheme.entity.SchemeFaqRepository;
import com.govscheme.scheme.entity.SchemeRawDataRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import com.govscheme.scheme.entity.SchemeStateRepository;
import com.govscheme.scheme.entity.SchemeTagRepository;
import com.govscheme.scheme.entity.SyncErrorRepository;
import com.govscheme.scheme.entity.SyncJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestMailConfig.class, SyncAdminTests.StubClientConfig.class})
class SyncAdminTests {

    /**
     * Pins the offline stub regardless of any MYSCHEME_API_KEY leaked into the
     * test environment, keeping admin sync tests deterministic and offline.
     */
    @org.springframework.boot.test.context.TestConfiguration
    static class StubClientConfig {
        @Bean
        @Primary
        MySchemeClient mySchemeClient() {
            return new StubMySchemeClient();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private UserAddressRepository addressRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeRawDataRepository rawDataRepository;

    @Autowired
    private SchemeTagRepository tagRepository;

    @Autowired
    private SchemeStateRepository stateRepository;

    @Autowired
    private SchemeFaqRepository faqRepository;

    @Autowired
    private SchemeDocumentRepository documentRepository;

    @Autowired
    private SchemeApplicationStepRepository stepRepository;

    @Autowired
    private SyncJobRepository jobRepository;

    @Autowired
    private SyncErrorRepository errorRepository;

    @BeforeEach
    void clean() {
        errorRepository.deleteAll();
        jobRepository.deleteAll();
        rawDataRepository.deleteAll();
        tagRepository.deleteAll();
        stateRepository.deleteAll();
        faqRepository.deleteAll();
        documentRepository.deleteAll();
        stepRepository.deleteAll();
        schemeRepository.deleteAll();
        addressRepository.deleteAll();
        profileRepository.deleteAll();
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
    void syncRequiresAdmin() throws Exception {
        String userToken = registerUser(uniqueEmail("citizen"));

        mockMvc.perform(post("/api/admin/schemes/sync").param("limit", "1"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/schemes/sync").param("limit", "1")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanTriggerSyncAndInspectJobs() throws Exception {
        String adminToken = login(createAdmin());

        MvcResult sync = mockMvc.perform(post("/api/admin/schemes/sync")
                .param("limit", "1")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.fetched").value(1))
            .andReturn();
        String jobId = objectMapper.readTree(sync.getResponse().getContentAsString())
            .get("data").get("id").asText();

        mockMvc.perform(get("/api/admin/sync/jobs")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(1));

        mockMvc.perform(get("/api/admin/sync/errors")
                .param("jobId", jobId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray());
    }
}
