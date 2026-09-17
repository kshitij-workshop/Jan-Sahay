package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class AgeRule implements EligibilityRule {

    @Override
    public String code() {
        return "AGE";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Integer min = context.criteria().getMinAge();
        Integer max = context.criteria().getMaxAge();
        if (min == null && max == null) {
            return RuleResult.skip(code());
        }
        LocalDate dob = context.profile().getDateOfBirth();
        if (dob == null) {
            return RuleResult.missing(code(), range(min, max),
                "Date of birth is needed to check the age requirement.",
                "personal.dateOfBirth");
        }
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (min != null && age < min) {
            return RuleResult.fail(code(), age, range(min, max),
                "Age " + age + " is below the minimum age " + min + ".");
        }
        if (max != null && age > max) {
            return RuleResult.fail(code(), age, range(min, max),
                "Age " + age + " is above the maximum age " + max + ".");
        }
        return RuleResult.pass(code(), age, range(min, max),
            "Age " + age + " falls within the required range " + range(min, max) + ".");
    }

    private String range(Integer min, Integer max) {
        return (min != null ? min : "any") + "-" + (max != null ? max : "any");
    }
}
