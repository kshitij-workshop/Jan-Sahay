package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GenderRule implements EligibilityRule {

    @Override
    public String code() {
        return "GENDER";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Set<String> genders = Rules.csv(context.criteria().getGenders());
        if (genders.isEmpty()) {
            return RuleResult.skip(code());
        }
        if (context.profile().getGender() == null) {
            return RuleResult.missing(code(), String.join(",", genders),
                "Gender is needed to check the eligibility requirement.", "personal.gender");
        }
        String userGender = context.profile().getGender().name();
        if (genders.contains(userGender)) {
            return RuleResult.pass(code(), userGender, String.join(",", genders),
                "Gender '" + userGender + "' meets the requirement.");
        }
        return RuleResult.fail(code(), userGender, String.join(",", genders),
            "Gender '" + userGender + "' does not meet the requirement.");
    }
}
