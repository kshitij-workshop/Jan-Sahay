package com.govscheme.eligibility.rule;

import com.govscheme.eligibility.EligibilityResult;
import com.govscheme.eligibility.EligibilityStatus;
import com.govscheme.profile.entity.CasteCategory;
import com.govscheme.profile.entity.Gender;
import com.govscheme.profile.entity.MaritalStatus;
import com.govscheme.profile.entity.Occupation;
import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserProfile;
import com.govscheme.eligibility.EligibilityEngine;
import com.govscheme.eligibility.entity.SchemeEligibility;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityRuleTests {

    private final EligibilityEngine engine = new EligibilityEngine(List.of(
        new AgeRule(), new StateRule(), new GenderRule(), new IncomeRule(),
        new CasteRule(), new OccupationRule(), new StudentRule(), new DisabilityRule(),
        new ResidenceRule(), new EmploymentRule(), new BplRule(), new MaritalStatusRule(),
        new MinorityRule()));

    private EligibilityRule.EvaluationContext ctx(UserProfile profile, UserAddress address,
                                                   SchemeEligibility criteria) {
        return new EligibilityRule.EvaluationContext(profile, address, criteria);
    }

    private UserProfile profile() {
        UserProfile profile = new UserProfile();
        profile.setFullName("Test User");
        profile.setDateOfBirth(LocalDate.now().minusYears(22));
        profile.setGender(Gender.MALE);
        profile.setAnnualIncome(150000L);
        profile.setCasteCategory(CasteCategory.OBC);
        profile.setOccupation(Occupation.STUDENT);
        profile.setStudentStatus(true);
        profile.setMaritalStatus(MaritalStatus.UNMARRIED);
        return profile;
    }

    private UserAddress address() {
        UserAddress address = new UserAddress();
        address.setState("Bihar");
        address.setDistrict("Patna");
        return address;
    }

    private SchemeEligibility criteria() {
        return new SchemeEligibility();
    }

    private RuleResult result(String code, EligibilityResult outcome) {
        return outcome.getRuleResults().stream()
            .filter(r -> r.getRule().equals(code))
            .findFirst()
            .orElseThrow();
    }

    @Test
    void ageWithinRangePasses() {
        SchemeEligibility criteria = criteria();
        criteria.setMinAge(18);
        criteria.setMaxAge(70);

        RuleResult result = new AgeRule().evaluate(ctx(profile(), address(), criteria));

        assertThat(result.getStatus()).isEqualTo(RuleResult.Status.PASS);
        assertThat(result.getUserValue()).isEqualTo(22);
        assertThat(result.getRequiredValue()).isEqualTo("18-70");
    }

    @Test
    void ageBelowMinimumFails() {
        UserProfile profile = profile();
        profile.setDateOfBirth(LocalDate.now().minusYears(17));
        SchemeEligibility criteria = criteria();
        criteria.setMinAge(18);
        criteria.setMaxAge(70);

        RuleResult result = new AgeRule().evaluate(ctx(profile, address(), criteria));

        assertThat(result.getStatus()).isEqualTo(RuleResult.Status.FAIL);
    }

    @Test
    void missingAgeIsInsufficientNotFailure() {
        UserProfile profile = profile();
        profile.setDateOfBirth(null);
        SchemeEligibility criteria = criteria();
        criteria.setMinAge(18);
        criteria.setMaxAge(70);

        EligibilityResult outcome = engine.evaluate(ctx(profile, address(), criteria));

        assertThat(result("AGE", outcome).getStatus()).isEqualTo(RuleResult.Status.MISSING);
        assertThat(outcome.getStatus()).isEqualTo(EligibilityStatus.INSUFFICIENT_INFORMATION);
        assertThat(outcome.getMissingFields()).contains("personal.dateOfBirth");
    }

    @Test
    void unconstrainedDimensionsSkip() {
        EligibilityResult outcome = engine.evaluate(ctx(profile(), address(), criteria()));

        assertThat(outcome.getRuleResults()).allMatch(r -> r.getStatus() == RuleResult.Status.SKIP);
        assertThat(outcome.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(outcome.getMissingFields()).isEmpty();
    }

    @Test
    void biharUserMatchesBiharScheme() {
        SchemeEligibility criteria = criteria();
        criteria.setStates("Bihar");

        RuleResult result = new StateRule().evaluate(ctx(profile(), address(), criteria));

        assertThat(result.getStatus()).isEqualTo(RuleResult.Status.PASS);
    }

    @Test
    void otherStateUserFailsBiharOnlyScheme() {
        UserAddress address = address();
        address.setState("Uttar Pradesh");
        SchemeEligibility criteria = criteria();
        criteria.setStates("Bihar");

        EligibilityResult outcome = engine.evaluate(ctx(profile(), address, criteria));

        assertThat(result("STATE", outcome).getStatus()).isEqualTo(RuleResult.Status.FAIL);
        assertThat(outcome.getStatus()).isEqualTo(EligibilityStatus.NOT_ELIGIBLE);
    }

    @Test
    void allIndiaSchemeMatchesAnyState() {
        UserAddress address = address();
        address.setState("Kerala");
        SchemeEligibility criteria = criteria();
        criteria.setStates("All India");

        RuleResult result = new StateRule().evaluate(ctx(profile(), address(), criteria));

        assertThat(result.getStatus()).isEqualTo(RuleResult.Status.PASS);
    }

    @Test
    void missingIncomeIsInsufficientNotFailure() {
        UserProfile profile = profile();
        profile.setAnnualIncome(null);
        SchemeEligibility criteria = criteria();
        criteria.setMaxAnnualIncome(200000L);

        EligibilityResult outcome = engine.evaluate(ctx(profile, address(), criteria));

        assertThat(result("INCOME", outcome).getStatus()).isEqualTo(RuleResult.Status.MISSING);
        assertThat(outcome.getStatus()).isEqualTo(EligibilityStatus.INSUFFICIENT_INFORMATION);
        assertThat(outcome.getMissingFields()).contains("economic.annualIncome");
    }

    @Test
    void incomeAboveLimitFails() {
        UserProfile profile = profile();
        profile.setAnnualIncome(500000L);
        SchemeEligibility criteria = criteria();
        criteria.setMaxAnnualIncome(200000L);

        RuleResult result = new IncomeRule().evaluate(ctx(profile, address(), criteria));

        assertThat(result.getStatus()).isEqualTo(RuleResult.Status.FAIL);
    }

    @Test
    void oneFailureDominatesMissingFields() {
        UserProfile profile = profile();
        profile.setAnnualIncome(null);
        profile.setCasteCategory(CasteCategory.GENERAL);
        SchemeEligibility criteria = criteria();
        criteria.setMaxAnnualIncome(200000L);
        criteria.setCasteCategories("SC,ST");

        EligibilityResult outcome = engine.evaluate(ctx(profile, address(), criteria));

        assertThat(outcome.getStatus()).isEqualTo(EligibilityStatus.NOT_ELIGIBLE);
        assertThat(outcome.getMissingFields()).contains("economic.annualIncome");
    }

    @Test
    void genderCasteOccupationStudentRules() {
        SchemeEligibility criteria = criteria();
        criteria.setGenders("MALE");
        criteria.setCasteCategories("OBC,SC");
        criteria.setOccupations("STUDENT");
        criteria.setRequireStudent(true);

        EligibilityResult outcome = engine.evaluate(ctx(profile(), address(), criteria));

        assertThat(outcome.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result("GENDER", outcome).getStatus()).isEqualTo(RuleResult.Status.PASS);
        assertThat(result("STUDENT", outcome).getStatus()).isEqualTo(RuleResult.Status.PASS);
    }

    @Test
    void disabilityPercentageEnforced() {
        UserProfile profile = profile();
        profile.setDisabilityStatus(true);
        profile.setDisabilityPercentage(30);
        SchemeEligibility criteria = criteria();
        criteria.setRequireDisabled(true);
        criteria.setMinDisabilityPercentage(40);

        assertThat(new DisabilityRule().evaluate(ctx(profile, address(), criteria)).getStatus())
            .isEqualTo(RuleResult.Status.FAIL);

        profile.setDisabilityPercentage(50);
        assertThat(new DisabilityRule().evaluate(ctx(profile, address(), criteria)).getStatus())
            .isEqualTo(RuleResult.Status.PASS);
    }

    @Test
    void booleanRulesReportMissingWhenUnknown() {
        UserProfile profile = profile();
        SchemeEligibility criteria = criteria();
        criteria.setRequireBpl(true);
        criteria.setRequireMinority(true);
        criteria.setMaritalStatuses("MARRIED");

        EligibilityResult outcome = engine.evaluate(ctx(profile(), address(), criteria));

        assertThat(result("BPL", outcome).getStatus()).isEqualTo(RuleResult.Status.MISSING);
        assertThat(result("MINORITY", outcome).getStatus()).isEqualTo(RuleResult.Status.MISSING);
        assertThat(result("MARITAL_STATUS", outcome).getStatus()).isEqualTo(RuleResult.Status.FAIL);
        assertThat(outcome.getMissingFields())
            .contains("economic.bplStatus", "social.minorityStatus");
    }
}
