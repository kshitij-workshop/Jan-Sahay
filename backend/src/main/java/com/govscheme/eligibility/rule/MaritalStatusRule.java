package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MaritalStatusRule implements EligibilityRule {

    @Override
    public String code() {
        return "MARITAL_STATUS";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Set<String> statuses = Rules.csv(context.criteria().getMaritalStatuses());
        if (statuses.isEmpty()) {
            return RuleResult.skip(code());
        }
        if (context.profile().getMaritalStatus() == null) {
            return RuleResult.missing(code(), String.join(",", statuses),
                "Marital status is needed to check the requirement.", "family.maritalStatus");
        }
        String actual = context.profile().getMaritalStatus().name();
        if (statuses.contains(actual)) {
            return RuleResult.pass(code(), actual, String.join(",", statuses),
                "Marital status '" + actual + "' meets the requirement.");
        }
        return RuleResult.fail(code(), actual, String.join(",", statuses),
            "Marital status '" + actual + "' does not meet the requirement.");
    }
}
