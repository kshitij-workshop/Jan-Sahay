package com.govscheme.eligibility.rule;

public class RuleResult {

    public enum Status {
        PASS,
        FAIL,
        MISSING,
        SKIP
    }

    private final String rule;
    private final Status status;
    private final Object userValue;
    private final String requiredValue;
    private final String message;
    private final String missingField;

    private RuleResult(String rule, Status status, Object userValue,
                       String requiredValue, String message, String missingField) {
        this.rule = rule;
        this.status = status;
        this.userValue = userValue;
        this.requiredValue = requiredValue;
        this.message = message;
        this.missingField = missingField;
    }

    public static RuleResult pass(String rule, Object userValue, String requiredValue, String message) {
        return new RuleResult(rule, Status.PASS, userValue, requiredValue, message, null);
    }

    public static RuleResult fail(String rule, Object userValue, String requiredValue, String message) {
        return new RuleResult(rule, Status.FAIL, userValue, requiredValue, message, null);
    }

    public static RuleResult missing(String rule, String requiredValue, String message, String missingField) {
        return new RuleResult(rule, Status.MISSING, null, requiredValue, message, missingField);
    }

    public static RuleResult skip(String rule) {
        return new RuleResult(rule, Status.SKIP, null, null, "Not constrained by this scheme.", null);
    }

    public String getRule() { return rule; }
    public Status getStatus() { return status; }
    public Object getUserValue() { return userValue; }
    public String getRequiredValue() { return requiredValue; }
    public String getMessage() { return message; }
    public String getMissingField() { return missingField; }
}
