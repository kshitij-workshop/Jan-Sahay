package com.govscheme.eligibility.dto;

import com.govscheme.eligibility.EligibilityResult;
import com.govscheme.eligibility.EligibilityStatus;
import com.govscheme.eligibility.rule.RuleResult;

import java.util.List;

public class EligibilityResponse {

    private String schemeId;
    private EligibilityStatus status;
    private List<RuleResultDto> rules;
    private List<String> missingFields;

    public static EligibilityResponse from(String schemeId, EligibilityResult result) {
        EligibilityResponse dto = new EligibilityResponse();
        dto.setSchemeId(schemeId);
        dto.setStatus(result.getStatus());
        dto.setRules(result.getRuleResults().stream().map(RuleResultDto::from).toList());
        dto.setMissingFields(result.getMissingFields());
        return dto;
    }

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public EligibilityStatus getStatus() { return status; }
    public void setStatus(EligibilityStatus status) { this.status = status; }

    public List<RuleResultDto> getRules() { return rules; }
    public void setRules(List<RuleResultDto> rules) { this.rules = rules; }

    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }

    public static class RuleResultDto {
        private String rule;
        private RuleResult.Status status;
        private Object userValue;
        private String requiredValue;
        private String message;
        private String missingField;

        public static RuleResultDto from(RuleResult result) {
            RuleResultDto dto = new RuleResultDto();
            dto.setRule(result.getRule());
            dto.setStatus(result.getStatus());
            dto.setUserValue(result.getUserValue());
            dto.setRequiredValue(result.getRequiredValue());
            dto.setMessage(result.getMessage());
            dto.setMissingField(result.getMissingField());
            return dto;
        }

        public String getRule() { return rule; }
        public void setRule(String rule) { this.rule = rule; }

        public RuleResult.Status getStatus() { return status; }
        public void setStatus(RuleResult.Status status) { this.status = status; }

        public Object getUserValue() { return userValue; }
        public void setUserValue(Object userValue) { this.userValue = userValue; }

        public String getRequiredValue() { return requiredValue; }
        public void setRequiredValue(String requiredValue) { this.requiredValue = requiredValue; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getMissingField() { return missingField; }
        public void setMissingField(String missingField) { this.missingField = missingField; }
    }
}
