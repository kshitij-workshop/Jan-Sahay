package com.govscheme.eligibility;

import com.govscheme.eligibility.rule.EligibilityRule;
import com.govscheme.eligibility.rule.RuleResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Runs every rule and aggregates deterministically. Rules are evaluated in
 * code order so explanations are stable across runs.
 */
@Component
public class EligibilityEngine {

    private final List<EligibilityRule> rules;

    public EligibilityEngine(List<EligibilityRule> rules) {
        this.rules = rules.stream()
            .sorted(Comparator.comparing(EligibilityRule::code))
            .toList();
    }

    public EligibilityResult evaluate(EligibilityRule.EvaluationContext context) {
        List<RuleResult> results = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        boolean failed = false;
        for (EligibilityRule rule : rules) {
            RuleResult result = rule.evaluate(context);
            results.add(result);
            if (result.getStatus() == RuleResult.Status.FAIL) {
                failed = true;
            } else if (result.getStatus() == RuleResult.Status.MISSING && result.getMissingField() != null) {
                missing.add(result.getMissingField());
            }
        }
        EligibilityStatus status = failed ? EligibilityStatus.NOT_ELIGIBLE
            : !missing.isEmpty() ? EligibilityStatus.INSUFFICIENT_INFORMATION
            : EligibilityStatus.ELIGIBLE;
        return new EligibilityResult(status, results, missing);
    }
}
