package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

@Component
public class BplRule implements EligibilityRule {

    @Override
    public String code() {
        return "BPL";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Boolean required = context.criteria().getRequireBpl();
        if (required == null) {
            return RuleResult.skip(code());
        }
        Boolean actual = context.profile().getBplStatus();
        if (actual == null) {
            return RuleResult.missing(code(), required ? "BPL" : "non-BPL",
                "BPL status is needed to check the requirement.", "economic.bplStatus");
        }
        if (actual.equals(required)) {
            return RuleResult.pass(code(), actual, required ? "BPL" : "non-BPL",
                required ? "Applicant holds BPL status as required."
                    : "Applicant is not BPL as required.");
        }
        return RuleResult.fail(code(), actual, required ? "BPL" : "non-BPL",
            required ? "Scheme requires BPL card holders." : "Scheme excludes BPL card holders.");
    }
}
