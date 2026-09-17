package com.govscheme.eligibility.rule;

import org.springframework.stereotype.Component;

@Component
public class StudentRule implements EligibilityRule {

    @Override
    public String code() {
        return "STUDENT";
    }

    @Override
    public RuleResult evaluate(EvaluationContext context) {
        Boolean required = context.criteria().getRequireStudent();
        if (required == null) {
            return RuleResult.skip(code());
        }
        Boolean actual = context.profile().getStudentStatus();
        if (actual == null) {
            return RuleResult.missing(code(), required ? "student" : "non-student",
                "Student status is needed to check the requirement.", "education.studentStatus");
        }
        if (actual.equals(required)) {
            return RuleResult.pass(code(), actual, required ? "student" : "non-student",
                required ? "Applicant is a student as required." : "Applicant is not a student as required.");
        }
        return RuleResult.fail(code(), actual, required ? "student" : "non-student",
            required ? "Scheme requires currently enrolled students."
                : "Scheme excludes currently enrolled students.");
    }
}
