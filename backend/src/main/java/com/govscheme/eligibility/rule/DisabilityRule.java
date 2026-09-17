package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

@Component
public class DisabilityRule implements EligibilityRule {

    @Override
    public String code() {
        return "DISABILITY";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Boolean required = context.criteria().getRequireDisabled();
        Integer minPct = context.criteria().getMinDisabilityPercentage();
        if (required == null && minPct == null) {
            return RuleResult.skip(code());
        }
        Boolean actual = context.profile().getDisabilityStatus();
        if (required != null) {
            if (actual == null) {
                return RuleResult.missing(code(), required ? "disabled" : "non-disabled",
                    "Disability status is needed to check the requirement.",
                    "social.disabilityStatus");
            }
            if (!actual.equals(required)) {
                return RuleResult.fail(code(), actual, required ? "disabled" : "non-disabled",
                    required ? "Scheme requires persons with disabilities."
                        : "Scheme excludes persons with disabilities.");
            }
        }
        if (minPct != null) {
            if (actual == null) {
                return RuleResult.missing(code(), ">=" + minPct + "%",
                    "Disability status is needed to check the percentage requirement.",
                    "social.disabilityStatus");
            }
            if (!actual) {
                return RuleResult.fail(code(), false, ">=" + minPct + "%",
                    "Scheme requires a disability of at least " + minPct + "%.");
            }
            Integer pct = context.profile().getDisabilityPercentage();
            if (pct == null) {
                return RuleResult.missing(code(), ">=" + minPct + "%",
                    "Disability percentage is needed to check the requirement.",
                    "social.disabilityPercentage");
            }
            if (pct < minPct) {
                return RuleResult.fail(code(), pct, ">=" + minPct + "%",
                    "Disability " + pct + "% is below the required " + minPct + "%.");
            }
            return RuleResult.pass(code(), pct, ">=" + minPct + "%",
                "Disability " + pct + "% meets the requirement.");
        }
        return RuleResult.pass(code(), actual, required ? "disabled" : "non-disabled",
            "Disability status meets the requirement.");
    }
}
