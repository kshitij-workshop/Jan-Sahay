package com.govscheme.profile.service;

import com.govscheme.profile.dto.ProfileCompleteness;
import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserProfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure profile-fullness math, kept static and framework-free so it is trivially
 * unit-testable. A blank string counts as missing. Disability percentage is
 * only required when disability is declared.
 */
public final class ProfileCompletenessCalculator {

    private ProfileCompletenessCalculator() {
    }

    public static ProfileCompleteness calculate(UserProfile profile, List<UserAddress> addresses) {
        Map<String, Integer> sections = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();

        sections.put("personal", fraction(
            isPresent(profile.getFullName(), "personal.fullName", missing),
            isPresent(profile.getDateOfBirth(), "personal.dateOfBirth", missing),
            isPresent(profile.getGender(), "personal.gender", missing)
        ));

        sections.put("address", addressFraction(addresses, missing));
        sections.put("education", fraction(
            isPresent(profile.getEducationLevel(), "education.educationLevel", missing),
            isPresent(profile.getStudentStatus(), "education.studentStatus", missing)
        ));
        sections.put("economic", fraction(
            isPresent(profile.getAnnualIncome(), "economic.annualIncome", missing),
            isPresent(profile.getIncomeCategory(), "economic.incomeCategory", missing)
        ));

        int socialTotal = 1;
        boolean socialPresent = isPresent(profile.getCasteCategory(), "social.casteCategory", missing);
        boolean disabilityPctPresent = false;
        if (Boolean.TRUE.equals(profile.getDisabilityStatus())) {
            socialTotal = 2;
            disabilityPctPresent = isPresent(profile.getDisabilityPercentage(),
                "social.disabilityPercentage", missing);
        }
        sections.put("social", percent(count(socialPresent) + count(disabilityPctPresent), socialTotal));

        sections.put("employment", fraction(
            isPresent(profile.getOccupation(), "employment.occupation", missing),
            isPresent(profile.getEmploymentStatus(), "employment.employmentStatus", missing)
        ));
        sections.put("family", fraction(
            isPresent(profile.getMaritalStatus(), "family.maritalStatus", missing)
        ));

        int overall = (int) Math.round(
            sections.values().stream().mapToInt(Integer::intValue).average().orElse(0.0));
        return new ProfileCompleteness(overall, sections, missing);
    }

    private static int addressFraction(List<UserAddress> addresses, List<String> missing) {
        if (addresses == null || addresses.isEmpty()) {
            missing.add("address.state");
            missing.add("address.district");
            missing.add("address.villageTown");
            missing.add("address.pincode");
            return 0;
        }
        UserAddress primary = addresses.stream()
            .filter(a -> Boolean.TRUE.equals(a.getPrimary()))
            .findFirst()
            .orElse(addresses.get(0));
        return fraction(
            isPresent(primary.getState(), "address.state", missing),
            isPresent(primary.getDistrict(), "address.district", missing),
            isPresent(primary.getVillageTown(), "address.villageTown", missing),
            isPresent(primary.getPincode(), "address.pincode", missing));
    }
    private static boolean isPresent(Object value, String field, List<String> missing) {
        boolean ok;
        if (value instanceof String s) {
            ok = !s.isBlank();
        } else {
            ok = value != null;
        }
        if (!ok) {
            missing.add(field);
        }
        return ok;
    }

    private static int fraction(boolean... checks) {
        int hit = 0;
        for (boolean c : checks) {
            if (c) {
                hit++;
            }
        }
        return percent(hit, checks.length);
    }

    private static int percent(int hit, int total) {
        if (total == 0) {
            return 100;
        }
        return (int) Math.round(hit * 100.0 / total);
    }

    private static int count(boolean b) {
        return b ? 1 : 0;
    }
}
