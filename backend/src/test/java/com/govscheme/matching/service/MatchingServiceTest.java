package com.govscheme.matching.service;

import com.govscheme.auth.TestMailConfig;
import com.govscheme.auth.entity.User;
import com.govscheme.auth.entity.UserRepository;
import com.govscheme.eligibility.entity.SchemeEligibility;
import com.govscheme.eligibility.entity.SchemeEligibilityRepository;
import com.govscheme.matching.entity.UserSchemeMatch;
import com.govscheme.matching.entity.UserSchemeMatchRepository;
import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserAddressRepository;
import com.govscheme.profile.entity.UserProfile;
import com.govscheme.profile.entity.UserProfileRepository;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeApplicationStepRepository;
import com.govscheme.scheme.entity.SchemeBeneficiaryRepository;
import com.govscheme.scheme.entity.SchemeCategoryRepository;
import com.govscheme.scheme.entity.SchemeDocumentRepository;
import com.govscheme.scheme.entity.SchemeFaqRepository;
import com.govscheme.scheme.entity.SchemeRawDataRepository;
import com.govscheme.scheme.entity.SchemeReferenceRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import com.govscheme.scheme.entity.SchemeStateRepository;
import com.govscheme.scheme.entity.SchemeTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class MatchingServiceTest {

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private UserAddressRepository addressRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeEligibilityRepository eligibilityRepository;

    @Autowired
    private UserSchemeMatchRepository matchRepository;

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
    private SchemeCategoryRepository categoryRepository;

    @Autowired
    private SchemeBeneficiaryRepository beneficiaryRepository;

    @Autowired
    private SchemeReferenceRepository referenceRepository;

    @Autowired
    private SchemeRawDataRepository rawDataRepository;

    private String userId;
    private String schemeId;

    @BeforeEach
    void cleanAndSeed() {
        matchRepository.deleteAll();
        eligibilityRepository.deleteAll();
        rawDataRepository.deleteAll();
        tagRepository.deleteAll();
        stateRepository.deleteAll();
        faqRepository.deleteAll();
        documentRepository.deleteAll();
        stepRepository.deleteAll();
        categoryRepository.deleteAll();
        beneficiaryRepository.deleteAll();
        referenceRepository.deleteAll();
        schemeRepository.deleteAll();
        User user = new User();
        user.setEmail("match-" + UUID.randomUUID() + "@example.com");
        user.setPhone("9" + UUID.randomUUID().toString().replace("-", "").substring(0, 9));
        user.setFullName("Match User");
        user.setPasswordHash("hash");
        user.setRole(User.Role.USER);
        user.setIsActive(true);
        user.setEmailVerified(true);
        user.setPhoneVerified(false);
        userRepository.save(user);
        userId = user.getId();

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setNationality("Indian");
        profile.setDateOfBirth(LocalDate.now().minusYears(22));
        profile.setAnnualIncome(150000L);
        profileRepository.save(profile);

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setState("Bihar");
        address.setDistrict("Patna");
        address.setPrimary(true);
        addressRepository.save(address);

        Scheme scheme = new Scheme();
        scheme.setSlug("match-scheme-" + UUID.randomUUID());
        scheme.setSchemeName("Match Scheme");
        scheme.setSource("TEST");
        schemeRepository.save(scheme);
        schemeId = scheme.getId();

        SchemeEligibility criteria = new SchemeEligibility();
        criteria.setSchemeId(schemeId);
        criteria.setStates("Bihar");
        criteria.setMaxAnnualIncome(200000L);
        eligibilityRepository.save(criteria);
    }

    @Test
    void recalculateStoresEligibleVerdictWithReason() {
        matchingService.recalculateForUser(userId);

        UserSchemeMatch match = matchRepository.findByUserIdAndSchemeId(userId, schemeId).orElseThrow();
        assertThat(match.getStatus()).isEqualTo(UserSchemeMatch.Status.ELIGIBLE);
        assertThat(match.getMatchReason()).contains("Passed");
        assertThat(match.getMissingInformation()).isNull();
        assertThat(match.getFirstMatchedAt()).isNotNull();
        assertThat(match.getLastCheckedAt()).isNotNull();
    }

    @Test
    void profileChangeFlipsVerdictAndPreservesFirstMatched() {
        matchingService.recalculateForUser(userId);
        var before = matchRepository.findByUserIdAndSchemeId(userId, schemeId).orElseThrow();

        UserAddress address = addressRepository.findByUserId(userId).get(0);
        address.setState("Kerala");
        addressRepository.save(address);
        matchingService.recalculateForUser(userId);

        var after = matchRepository.findByUserIdAndSchemeId(userId, schemeId).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(UserSchemeMatch.Status.NOT_ELIGIBLE);
        assertThat(after.getMatchReason()).contains("Kerala");
        assertThat(after.getFirstMatchedAt()).isEqualTo(before.getFirstMatchedAt());
        assertThat(matchRepository.findByUserId(userId)).hasSize(1);
    }

    @Test
    void recalculateForSchemeCoversAllUsers() {
        matchingService.recalculateForScheme(schemeId);

        assertThat(matchRepository.findBySchemeId(schemeId)).hasSize(1);
    }
}
