package com.govscheme.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.auth.entity.User;
import com.govscheme.auth.entity.UserRepository;
import com.govscheme.auth.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@example.com";
    }

    private String register(String email, String phone) throws Exception {
        Map<String, String> body = Map.of(
            "email", email,
            "phone", phone,
            "password", "Password123",
            "fullName", "Test User"
        );
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.user.role").value("USER"))
            .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    private String login(String identifier) throws Exception {
        Map<String, String> body = Map.of("identifier", identifier, "password", "Password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("data").get("accessToken").asText();
    }

    private String loginForRefreshToken(String identifier) throws Exception {
        Map<String, String> body = Map.of("identifier", identifier, "password", "Password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("data").get("refreshToken").asText();
    }

    @Test
    void registerCreatesUnverifiedUserAndSendsVerificationEmail() throws Exception {
        String email = uniqueEmail("verify");
        register(email, "9000000001");

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.getEmailVerified()).isFalse();
        assertThat(user.getEmailVerificationToken()).isNotBlank();
        assertThat(user.getEmailVerificationTokenExpiry()).isAfter(Instant.now());
        assertThat(user.getRole()).isEqualTo(User.Role.USER);
        verify(emailService).sendEmailVerification(eq(email), eq("Test User"), anyString());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String email = uniqueEmail("dup");
        register(email, "9000000002");

        Map<String, String> body = Map.of(
            "email", email,
            "phone", "9000000003",
            "password", "Password123",
            "fullName", "Test User"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        Map<String, String> body = Map.of(
            "email", "not-an-email",
            "phone", "123",
            "password", "short",
            "fullName", ""
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void loginSucceedsAndRejectsWrongPassword() throws Exception {
        String email = uniqueEmail("login");
        register(email, "9000000004");

        login(email);

        Map<String, String> bad = Map.of("identifier", email, "password", "WrongPass123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotatesTokensAndRejectsAccessToken() throws Exception {
        String email = uniqueEmail("refresh");
        String accessToken = register(email, "9000000005");
        String refreshToken = loginForRefreshToken(email);

        // Valid refresh token rotates the pair.
        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer " + refreshToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists());

        // An access token must not be usable as a refresh token.
        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    void meRequiresAccessToken() throws Exception {
        String email = uniqueEmail("me");
        String accessToken = register(email, "9000000006");
        String refreshToken = loginForRefreshToken(email);

        // No token -> denied.
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isForbidden());

        // Refresh token must not grant API access.
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + refreshToken))
            .andExpect(status().isForbidden());

        // Access token works and resolves via SecurityContext principal.
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(email));
    }

    @Test
    void verifyEmailHappyPathAndInvalidToken() throws Exception {
        String email = uniqueEmail("verifyok");
        register(email, "9000000007");

        User user = userRepository.findByEmail(email).orElseThrow();
        String token = user.getEmailVerificationToken();

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", token))))
            .andExpect(status().isOk());

        User verified = userRepository.findByEmail(email).orElseThrow();
        assertThat(verified.getEmailVerified()).isTrue();
        assertThat(verified.getEmailVerificationToken()).isNull();

        // Reusing the same token must fail.
        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", token))))
            .andExpect(status().isBadRequest());

        // Garbage token must fail.
        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", "does-not-exist"))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyEmailRejectsExpiredToken() throws Exception {
        String email = uniqueEmail("expired");
        register(email, "9000000008");

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setEmailVerificationTokenExpiry(Instant.now().minusSeconds(60));
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", user.getEmailVerificationToken()))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void resendVerificationSendsNewToken() throws Exception {
        String email = uniqueEmail("resend");
        register(email, "9000000009");

        mockMvc.perform(post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email))))
            .andExpect(status().isOk());

        verify(emailService).sendEmailVerification(eq(email), eq("Test User"), anyString());
    }

    @Test
    void adminPingEnforcesRoles() throws Exception {
        String userEmail = uniqueEmail("citizen");
        String userAccess = register(userEmail, "9000000010");

        User admin = new User();
        admin.setEmail(uniqueEmail("admin"));
        admin.setPhone("9000000011");
        admin.setFullName("Admin User");
        admin.setPasswordHash(passwordEncoder.encode("Password123"));
        admin.setRole(User.Role.ADMIN);
        admin.setIsActive(true);
        admin.setEmailVerified(true);
        admin.setPhoneVerified(false);
        userRepository.save(admin);
        String adminAccess = login(admin.getEmail());

        // Anonymous -> denied.
        mockMvc.perform(get("/api/admin/ping"))
            .andExpect(status().isForbidden());

        // USER -> denied (both URL rule and @PreAuthorize).
        mockMvc.perform(get("/api/admin/ping")
                .header("Authorization", "Bearer " + userAccess))
            .andExpect(status().isForbidden());

        // ADMIN -> allowed.
        mockMvc.perform(get("/api/admin/ping")
                .header("Authorization", "Bearer " + adminAccess))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("OK"));
    }
}
