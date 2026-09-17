package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CasteRule implements EligibilityRule {

    @Override
    public String code() {
        return "CASTE";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Set<String> castes = Rules.csv(context.criteria().getCasteCategories());
        if (castes.isEmpty()) {
            return RuleResult.skip(code());
        }
        if (context.profile().getCasteCategory() == null) {
            return RuleResult.missing(code(), String.join(",", castes),
                "Caste/category is needed to check the requirement.", "social.casteCategory");
        }
        String userCaste = context.profile().getCasteCategory().name();
        if (castes.contains(userCaste)) {
            return RuleResult.pass(code(), userCaste, String.join(",", castes),
                "Category '" + userCaste + "' meets the requirement.");
        }
        return RuleResult.fail(code(), userCaste, String.join(",", castes),
            "Category '" + userCaste + "' does not meet the requirement.");
    }
}
