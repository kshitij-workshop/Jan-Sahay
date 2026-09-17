package com.govscheme.profile.service;

import com.govscheme.profile.dto.ProfileCompleteness;
import com.govscheme.profile.entity.CasteCategory;
import com.govscheme.profile.entity.EducationLevel;
import com.govscheme.profile.entity.EmploymentStatus;
import com.govscheme.profile.entity.Gender;
import com.govscheme.profile.entity.IncomeCategory;
import com.govscheme.profile.entity.MaritalStatus;
import com.govscheme.profile.entity.Occupation;
import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserProfile;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileCompletenessCalculatorTest {

    @Test
    void emptyProfileScoresZeroWithAllMissingFields() {
        ProfileCompleteness result =
            ProfileCompletenessCalculator.calculate(new UserProfile(), List.of());

        assertThat(result.getOverallPercent()).isEqualTo(0);
        assertThat(result.getMissingFields()).containsExactlyInAnyOrder(
            "personal.fullName", "personal.dateOfBirth", "personal.gender",
            "address.state", "address.district", "address.villageTown", "address.pincode",
            "education.educationLevel", "education.studentStatus",
            "economic.annualIncome", "economic.incomeCategory",
            "social.casteCategory",
            "employment.occupation", "employment.employmentStatus",
            "family.maritalStatus"
        );
        assertThat(result.getSectionPercents()).hasSize(7);
    }

    @Test
    void fullProfileScoresHundred() {
        ProfileCompleteness result =
            ProfileCompletenessCalculator.calculate(fullProfile(), List.of(fullAddress()));

        assertThat(result.getOverallPercent()).isEqualTo(100);
        assertThat(result.getMissingFields()).isEmpty();
    }

    @Test
    void blankStringsCountAsMissing() {
        UserProfile profile = fullProfile();
        profile.setFullName("   ");
        ProfileCompleteness result =
            ProfileCompletenessCalculator.calculate(profile, List.of(fullAddress()));

        assertThat(result.getMissingFields()).contains("personal.fullName");
        assertThat(result.getSectionPercents().get("personal")).isEqualTo(67);
    }

    @Test
    void disabilityPercentageOnlyRequiredWhenDisabled() {
        UserProfile withoutDisability = fullProfile();
        withoutDisability.setDisabilityStatus(false);
        withoutDisability.setDisabilityPercentage(null);

        ProfileCompleteness ok =
            ProfileCompletenessCalculator.calculate(withoutDisability, List.of(fullAddress()));
        assertThat(ok.getMissingFields()).doesNotContain("social.disabilityPercentage");
        assertThat(ok.getSectionPercents().get("social")).isEqualTo(100);

        UserProfile withDisability = fullProfile();
        withDisability.setDisabilityStatus(true);
        withDisability.setDisabilityPercentage(null);

        ProfileCompleteness incomplete =
            ProfileCompletenessCalculator.calculate(withDisability, List.of(fullAddress()));
        assertThat(incomplete.getMissingFields()).contains("social.disabilityPercentage");
        assertThat(incomplete.getSectionPercents().get("social")).isEqualTo(50);
    }

    @Test
    void primaryAddressWinsOverFirstAddress() {
        UserAddress secondary = fullAddress();
        secondary.setPrimary(false);
        secondary.setState(null);
        UserAddress primary = fullAddress();
        primary.setPrimary(true);

        ProfileCompleteness result =
            ProfileCompletenessCalculator.calculate(fullProfile(), List.of(secondary, primary));

        assertThat(result.getSectionPercents().get("address")).isEqualTo(100);
        assertThat(result.getMissingFields()).doesNotContain("address.state");
    }

    private UserProfile fullProfile() {
        UserProfile profile = new UserProfile();
        profile.setFullName("Rahul Kumar");
        profile.setDateOfBirth(LocalDate.of(2004, 8, 15));
        profile.setGender(Gender.MALE);
        profile.setNationality("Indian");
        profile.setEducationLevel(EducationLevel.GRADUATE);
        profile.setStudentStatus(false);
        profile.setAnnualIncome(150000L);
        profile.setIncomeCategory(IncomeCategory.APL);
        profile.setCasteCategory(CasteCategory.OBC);
        profile.setDisabilityStatus(false);
        profile.setOccupation(Occupation.FARMER);
        profile.setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        profile.setMaritalStatus(MaritalStatus.UNMARRIED);
        return profile;
    }

    private UserAddress fullAddress() {
        UserAddress address = new UserAddress();
        address.setPrimary(true);
        address.setState("Bihar");
        address.setDistrict("Sheikhpura");
        address.setVillageTown("Sheikhpura");
        address.setPincode("811105");
        return address;
    }
}
