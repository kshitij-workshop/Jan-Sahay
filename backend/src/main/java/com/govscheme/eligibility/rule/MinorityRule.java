package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

@Component
public class MinorityRule implements EligibilityRule {

    @Override
    public String code() {
        return "MINORITY";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Boolean required = context.criteria().getRequireMinority();
        if (required == null) {
            return RuleResult.skip(code());
        }
        Boolean actual = context.profile().getMinorityStatus();
        if (actual == null) {
            return RuleResult.missing(code(), required ? "minority" : "non-minority",
                "Minority status is needed to check the requirement.", "social.minorityStatus");
        }
        if (actual.equals(required)) {
            return RuleResult.pass(code(), actual, required ? "minority" : "non-minority",
                "Minority status meets the requirement.");
        }
        return RuleResult.fail(code(), actual, required ? "minority" : "non-minority",
            required ? "Scheme requires minority community applicants."
                : "Scheme excludes minority community applicants.");
    }
}
