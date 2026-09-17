package com.govscheme.matching.service;

import com.govscheme.auth.entity.UserRepository;
import com.govscheme.eligibility.EligibilityEngine;
import com.govscheme.eligibility.EligibilityResult;
import com.govscheme.eligibility.EligibilityStatus;
import com.govscheme.eligibility.entity.SchemeEligibility;
import com.govscheme.eligibility.entity.SchemeEligibilityRepository;
import com.govscheme.eligibility.rule.EligibilityRule;
import com.govscheme.eligibility.rule.RuleResult;
import com.govscheme.matching.entity.UserSchemeMatch;
import com.govscheme.matching.entity.UserSchemeMatchRepository;
import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserAddressRepository;
import com.govscheme.profile.entity.UserProfile;
import com.govscheme.profile.entity.UserProfileRepository;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Persists per-user, per-scheme eligibility verdicts. Recalculation is
 * triggered by profile writes and scheme syncs/imports; verdicts always come
 * from the deterministic engine, never from cached assumptions.
 * <p>
 * MVP scale note: recalculation iterates users × schemes in calling threads.
 * At larger scale this moves to batched/async jobs behind the same methods.
 */
@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final EligibilityEngine engine;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserAddressRepository addressRepository;
    private final SchemeRepository schemeRepository;
    private final SchemeEligibilityRepository eligibilityRepository;
    private final UserSchemeMatchRepository matchRepository;

    public MatchingService(EligibilityEngine engine,
                           UserRepository userRepository,
                           UserProfileRepository profileRepository,
                           UserAddressRepository addressRepository,
                           SchemeRepository schemeRepository,
                           SchemeEligibilityRepository eligibilityRepository,
                           UserSchemeMatchRepository matchRepository) {
        this.engine = engine;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.addressRepository = addressRepository;
        this.schemeRepository = schemeRepository;
        this.eligibilityRepository = eligibilityRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public void recalculateForUser(String userId) {
        UserProfile profile = loadProfile(userId);
        List<UserAddress> addresses = addressRepository.findByUserId(userId);
        List<Scheme> schemes = schemeRepository.findAll();
        int changed = 0;
        for (Scheme scheme : schemes) {
            if (evaluateAndStore(userId, profile, addresses, scheme)) {
                changed++;
            }
        }
        log.info("MATCHING_RECALCULATED userId={} schemes={} changed={}", userId, schemes.size(), changed);
    }

    @Transactional
    public void recalculateForScheme(String schemeId) {
        Scheme scheme = schemeRepository.findById(schemeId).orElse(null);
        if (scheme == null) {
            return;
        }
        // MVP: iterate all users. Extract to batched jobs when user counts grow.
        List<String> userIds = userRepository.findAll().stream()
            .map(com.govscheme.auth.entity.User::getId)
            .toList();
        int changed = 0;
        for (String userId : userIds) {
            if (evaluateAndStore(userId, scheme)) {
                changed++;
            }
        }
        log.info("MATCHING_RECALCULATED schemeId={} users={} changed={}", schemeId, userIds.size(), changed);
    }

    @Transactional
    public void recalculateSchemesSyncedSince(Instant since) {
        List<Scheme> schemes = schemeRepository.findByLastSyncedAtAfter(since);
        for (Scheme scheme : schemes) {
            try {
                recalculateForScheme(scheme.getId());
            } catch (Exception e) {
                log.warn("MATCHING_SCHEME_FAILED schemeId={} message={}", scheme.getId(), e.getMessage());
            }
        }
    }

    private UserProfile loadProfile(String userId) {
        return profileRepository.findById(userId).orElseGet(() -> {
            UserProfile fresh = new UserProfile();
            fresh.setUserId(userId);
            fresh.setNationality("Indian");
            return fresh;
        });
    }

    private boolean evaluateAndStore(String userId, Scheme scheme) {
        return evaluateAndStore(userId, loadProfile(userId),
            addressRepository.findByUserId(userId), scheme);
    }

    private boolean evaluateAndStore(String userId, UserProfile profile,
                                     List<UserAddress> addresses, Scheme scheme) {
        UserAddress primary = addresses.stream()
            .filter(a -> Boolean.TRUE.equals(a.getPrimary()))
            .findFirst()
            .orElse(addresses.isEmpty() ? null : addresses.get(0));
        SchemeEligibility criteria = eligibilityRepository.findById(scheme.getId())
            .orElseGet(SchemeEligibility::new);

        EligibilityResult result = engine.evaluate(
            new EligibilityRule.EvaluationContext(profile, primary, criteria));

        UserSchemeMatch.Status status = switch (result.getStatus()) {
            case ELIGIBLE -> UserSchemeMatch.Status.ELIGIBLE;
            case NOT_ELIGIBLE -> UserSchemeMatch.Status.NOT_ELIGIBLE;
            case INSUFFICIENT_INFORMATION -> UserSchemeMatch.Status.INSUFFICIENT_INFORMATION;
        };

        UserSchemeMatch match = matchRepository.findByUserIdAndSchemeId(userId, scheme.getId())
            .orElseGet(() -> {
                UserSchemeMatch fresh = new UserSchemeMatch();
                fresh.setUserId(userId);
                fresh.setSchemeId(scheme.getId());
                fresh.setFirstMatchedAt(Instant.now());
                return fresh;
            });
        boolean changed = match.getId() == null || match.getStatus() != status;
        match.setStatus(status);
        match.setMatchReason(reason(result));
        match.setMissingInformation(result.getMissingFields().isEmpty()
            ? null : String.join(",", result.getMissingFields()));
        match.setLastCheckedAt(Instant.now());
        matchRepository.save(match);
        return changed;
    }

    private String reason(EligibilityResult result) {
        List<RuleResult> evaluated = result.getRuleResults().stream()
            .filter(r -> r.getStatus() != RuleResult.Status.SKIP)
            .toList();
        if (evaluated.isEmpty()) {
            return "No machine-readable constraints published for this scheme.";
        }
        if (result.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
            return "Failed: " + evaluated.stream()
                .filter(r -> r.getStatus() == RuleResult.Status.FAIL)
                .map(RuleResult::getMessage)
                .collect(Collectors.joining("; "));
        }
        if (result.getStatus() == EligibilityStatus.INSUFFICIENT_INFORMATION) {
            return "Needs: " + String.join(", ", result.getMissingFields());
        }
        List<String> passed = evaluated.stream()
            .filter(r -> r.getStatus() == RuleResult.Status.PASS)
            .map(r -> r.getRule().toLowerCase().replace('_', ' '))
            .toList();
        return "Passed " + passed.size() + " of " + evaluated.size()
            + " checks: " + String.join(", ", passed) + ".";
    }
}
