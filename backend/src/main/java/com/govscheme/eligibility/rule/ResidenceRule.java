package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ResidenceRule implements EligibilityRule {

    @Override
    public String code() {
        return "RESIDENCE";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Set<String> areas = Rules.csv(context.criteria().getAreaTypes());
        if (areas.isEmpty()) {
            return RuleResult.skip(code());
        }
        if (context.address() == null || context.address().getAreaType() == null) {
            return RuleResult.missing(code(), String.join(",", areas),
                "Rural/urban residence is needed to check the requirement.", "address.areaType");
        }
        String userArea = context.address().getAreaType().name();
        if (areas.contains(userArea)) {
            return RuleResult.pass(code(), userArea, String.join(",", areas),
                "Residence '" + userArea + "' meets the requirement.");
        }
        return RuleResult.fail(code(), userArea, String.join(",", areas),
            "Residence '" + userArea + "' does not meet the requirement.");
    }
}
