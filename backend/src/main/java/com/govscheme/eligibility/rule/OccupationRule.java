package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OccupationRule implements EligibilityRule {

    @Override
    public String code() {
        return "OCCUPATION";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Set<String> occupations = Rules.csv(context.criteria().getOccupations());
        if (occupations.isEmpty()) {
            return RuleResult.skip(code());
        }
        if (context.profile().getOccupation() == null) {
            return RuleResult.missing(code(), String.join(",", occupations),
                "Occupation is needed to check the requirement.", "employment.occupation");
        }
        String userOccupation = context.profile().getOccupation().name();
        if (occupations.contains(userOccupation)) {
            return RuleResult.pass(code(), userOccupation, String.join(",", occupations),
                "Occupation '" + userOccupation + "' meets the requirement.");
        }
        return RuleResult.fail(code(), userOccupation, String.join(",", occupations),
            "Occupation '" + userOccupation + "' does not meet the requirement.");
    }
}
