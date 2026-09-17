package com.govscheme.eligibility.rule;

import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserProfile;
import com.govscheme.eligibility.entity.SchemeEligibility;

/**
 * One deterministic eligibility dimension. Rules never guess: a dimension the
 * scheme does not constrain yields SKIP; a constrained dimension with no user
 * data yields MISSING (never FAIL). AI layers may rephrase messages but must
 * never change statuses.
 */
public interface EligibilityRule {

    /** Stable code, e.g. "AGE". Used for ordering and explanations. */
    String code();

    RuleResult evaluate(EvaluationContext context);

    record EvaluationContext(UserProfile profile, UserAddress address, SchemeEligibility criteria) {
    }
}
