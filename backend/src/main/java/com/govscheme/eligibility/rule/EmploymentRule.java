package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EmploymentRule implements EligibilityRule {

    @Override
    public String code() {
        return "EMPLOYMENT";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Set<String> statuses = Rules.csv(context.criteria().getEmploymentStatuses());
        Boolean requireGovt = context.criteria().getRequireGovtEmployee();
        if (statuses.isEmpty() && requireGovt == null) {
            return RuleResult.skip(code());
        }
        if (!statuses.isEmpty()) {
            if (context.profile().getEmploymentStatus() == null) {
                return RuleResult.missing(code(), String.join(",", statuses),
                    "Employment status is needed to check the requirement.",
                    "employment.employmentStatus");
            }
            String actual = context.profile().getEmploymentStatus().name();
            if (!statuses.contains(actual)) {
                return RuleResult.fail(code(), actual, String.join(",", statuses),
                    "Employment status '" + actual + "' does not meet the requirement.");
            }
        }
        if (requireGovt != null) {
            Boolean actual = context.profile().getGovtEmployee();
            if (actual == null) {
                return RuleResult.missing(code(), requireGovt ? "govt-employee" : "non-govt-employee",
                    "Government employment status is needed.", "employment.govtEmployee");
            }
            if (!actual.equals(requireGovt)) {
                return RuleResult.fail(code(), actual, requireGovt ? "govt-employee" : "non-govt-employee",
                    requireGovt ? "Scheme requires government employees."
                        : "Scheme excludes government employees.");
            }
        }
        return RuleResult.pass(code(),
            context.profile().getEmploymentStatus() != null
                ? context.profile().getEmploymentStatus().name() : null,
            describe(statuses, requireGovt), "Employment meets the requirement.");
    }

    private String describe(Set<String> statuses, Boolean requireGovt) {
        String base = statuses.isEmpty() ? "any status" : String.join(",", statuses);
        if (requireGovt == null) {
            return base;
        }
        return base + (requireGovt ? " + govt-employee" : " + non-govt-employee");
    }
}
