package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

@Component
public class IncomeRule implements EligibilityRule {

    @Override
    public String code() {
        return "INCOME";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Long max = context.criteria().getMaxAnnualIncome();
        if (max == null) {
            return RuleResult.skip(code());
        }
        Long income = context.profile().getAnnualIncome();
        if (income == null) {
            return RuleResult.missing(code(), "<=" + max,
                "Annual income is needed to check the income limit.", "economic.annualIncome");
        }
        if (income <= max) {
            return RuleResult.pass(code(), income, "<=" + max,
                "Annual income " + income + " is within the limit " + max + ".");
        }
        return RuleResult.fail(code(), income, "<=" + max,
            "Annual income " + income + " exceeds the limit " + max + ".");
    }
}
