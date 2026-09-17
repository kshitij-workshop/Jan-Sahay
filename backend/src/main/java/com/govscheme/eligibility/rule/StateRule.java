package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class StateRule implements EligibilityRule {

    @Override
    public String code() {
        return "STATE";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Set<String> states = Rules.csv(context.criteria().getStates());
        if (states.isEmpty()) {
            return RuleResult.skip(code());
        }
        if (context.address() == null || Rules.blank(context.address().getState())) {
            return RuleResult.missing(code(), String.join(",", states),
                "State is needed to check the residence requirement.", "address.state");
        }
        String userState = context.address().getState().trim();
        if (states.contains("ALL_INDIA") || states.contains(userState.toUpperCase().replace(' ', '_'))) {
            return RuleResult.pass(code(), userState, String.join(",", states),
                "State '" + userState + "' is covered by this scheme.");
        }
        return RuleResult.fail(code(), userState, String.join(",", states),
            "State '" + userState + "' is not covered by this scheme.");
    }
}
