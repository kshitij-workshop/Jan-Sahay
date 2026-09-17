package com.govscheme.eligibility.service;

import com.govscheme.auth.TestMailConfig;
import com.govscheme.eligibility.EligibilityEngine;
import com.govscheme.eligibility.EligibilityResult;
import com.govscheme.eligibility.EligibilityStatus;
import com.govscheme.eligibility.entity.SchemeEligibility;
import com.govscheme.eligibility.entity.SchemeEligibilityRepository;
import com.govscheme.eligibility.rule.EligibilityRule;
import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserProfile;
import com.govscheme.matching.entity.UserSchemeMatchRepository;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class CriteriaBootstrapTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CriteriaBootstrapService bootstrapService;

    @Autowired
    private EligibilityEngine engine;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeEligibilityRepository eligibilityRepository;

    @Autowired
    private UserSchemeMatchRepository matchRepository;

    private String biharSchemeId;
    private String plainSchemeId;
    private String curatedSchemeId;

    @BeforeEach
    void seed() {
        matchRepository.deleteAll();
        eligibilityRepository.deleteAll();
        schemeRepository.deleteAll();
        biharSchemeId = saveScheme("bihar-schol-" + UUID.randomUUID(), "Bihar Scholarship",
            "Education Department, Bihar");
        plainSchemeId = saveScheme("pm-central-" + UUID.randomUUID(), "PM Central Scheme",
            "Ministry of Finance");
        curatedSchemeId = saveScheme("bihar-curated-" + UUID.randomUUID(), "Bihar Curated",
            "Dept of Bihar Welfare");
        SchemeEligibility existing = new SchemeEligibility();
        existing.setSchemeId(curatedSchemeId);
        existing.setStates("Bihar,Uttar Pradesh");
        eligibilityRepository.save(existing);
    }

    private String saveScheme(String slug, String name, String ministry) {
        Scheme scheme = new Scheme();
        scheme.setSlug(slug);
        scheme.setSchemeName(name);
        scheme.setNodalMinistry(ministry);
        scheme.setSource("TEST");
        schemeRepository.save(scheme);
        return scheme.getId();
    }

    @Test
    void bootstrapCuratesOnlyExplicitlyBiharScopedSchemes() {
        CriteriaBootstrapService.BootstrapSummary summary = bootstrapService.bootstrap();

        assertThat(summary.scanned()).isEqualTo(3);
        assertThat(summary.curated()).isEqualTo(1);
        assertThat(summary.alreadyCurated()).isEqualTo(1);
        assertThat(summary.skipped()).isEqualTo(1);

        assertThat(eligibilityRepository.findById(biharSchemeId))
            .isPresent()
            .get()
            .extracting(SchemeEligibility::getStates)
            .isEqualTo("Bihar");
        assertThat(eligibilityRepository.findById(plainSchemeId)).isEmpty();
        // Existing curated rows are never overwritten.
        assertThat(eligibilityRepository.findById(curatedSchemeId).orElseThrow().getStates())
            .isEqualTo("Bihar,Uttar Pradesh");
    }

    @Test
    void curatedSchemeEvaluatesForBiharAddress() {
        bootstrapService.bootstrap();

        UserProfile profile = new UserProfile();
        UserAddress address = new UserAddress();
        address.setState("Bihar");
        SchemeEligibility criteria =
            eligibilityRepository.findById(biharSchemeId).orElseThrow();

        EligibilityResult result = engine.evaluate(
            new EligibilityRule.EvaluationContext(profile, address, criteria));

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void bootstrapEndpointRequiresAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/schemes/criteria/bootstrap"))
            .andExpect(status().isForbidden());
    }
}
