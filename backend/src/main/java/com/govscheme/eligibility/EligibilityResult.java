package com.govscheme.eligibility;

import com.govscheme.eligibility.rule.RuleResult;

import java.util.List;

/**
 * Deterministic outcome. Any FAIL dominates; otherwise any MISSING yields
 * INSUFFICIENT_INFORMATION with the exact absent fields. Zero evaluated rules
 * (nothing curated) is also INSUFFICIENT_INFORMATION — unknown is never a
 * pass. Only all-PASS with at least one evaluated rule is ELIGIBLE.
 */
public class EligibilityResult {

    private final EligibilityStatus status;
    private final List<RuleResult> ruleResults;
    private final List<String> missingFields;

    public EligibilityResult(EligibilityStatus status, List<RuleResult> ruleResults,
                             List<String> missingFields) {
        this.status = status;
        this.ruleResults = ruleResults;
        this.missingFields = missingFields;
    }

    public EligibilityStatus getStatus() { return status; }
    public List<RuleResult> getRuleResults() { return ruleResults; }
    public List<String> getMissingFields() { return missingFields; }
}
