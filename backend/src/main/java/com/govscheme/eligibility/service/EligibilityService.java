package com.govscheme.eligibility.service;

import com.govscheme.auth.entity.UserRepository;
import com.govscheme.common.exception.ResourceNotFoundException;
import com.govscheme.eligibility.EligibilityEngine;
import com.govscheme.eligibility.EligibilityResult;
import com.govscheme.eligibility.entity.SchemeEligibility;
import com.govscheme.eligibility.entity.SchemeEligibilityRepository;
import com.govscheme.eligibility.rule.EligibilityRule;
import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserAddressRepository;
import com.govscheme.profile.entity.UserProfile;
import com.govscheme.profile.entity.UserProfileRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Evaluates the authenticated user's stored profile against one scheme's
 * curated criteria. No criteria row means "no machine-readable constraints":
 * every rule skips and the outcome reflects profile completeness only
 * (ELIGIBLE when nothing is missing, else INSUFFICIENT_INFORMATION) —
 * never a fabricated FAIL.
 */
@Service
public class EligibilityService {

    private static final Logger log = LoggerFactory.getLogger(EligibilityService.class);

    private final EligibilityEngine engine;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserAddressRepository addressRepository;
    private final SchemeRepository schemeRepository;
    private final SchemeEligibilityRepository eligibilityRepository;

    public EligibilityService(EligibilityEngine engine,
                              UserRepository userRepository,
                              UserProfileRepository profileRepository,
                              UserAddressRepository addressRepository,
                              SchemeRepository schemeRepository,
                              SchemeEligibilityRepository eligibilityRepository) {
        this.engine = engine;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.addressRepository = addressRepository;
        this.schemeRepository = schemeRepository;
        this.eligibilityRepository = eligibilityRepository;
    }

    @Transactional(readOnly = true)
    public EligibilityResult evaluate(String username, String schemeId) {
        if (!schemeRepository.existsById(schemeId)) {
            throw new ResourceNotFoundException("Scheme", "id", schemeId);
        }
        String userId = userRepository.findByEmailOrPhone(username, username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username))
            .getId();

        UserProfile profile = profileRepository.findById(userId).orElseGet(() -> {
            UserProfile fresh = new UserProfile();
            fresh.setUserId(userId);
            fresh.setNationality("Indian");
            return fresh;
        });
        List<UserAddress> addresses = addressRepository.findByUserId(userId);
        UserAddress primary = addresses.stream()
            .filter(a -> Boolean.TRUE.equals(a.getPrimary()))
            .findFirst()
            .orElse(addresses.isEmpty() ? null : addresses.get(0));

        SchemeEligibility criteria = eligibilityRepository.findById(schemeId)
            .orElseGet(SchemeEligibility::new);

        EligibilityResult result = engine.evaluate(
            new EligibilityRule.EvaluationContext(profile, primary, criteria));
        log.info("ELIGIBILITY_CALCULATED schemeId={} status={} missingCount={}",
            schemeId, result.getStatus(), result.getMissingFields().size());
        return result;
    }
}
