package com.govscheme.profile.dto;

import java.util.List;
import java.util.Map;

/**
 * Profile fullness breakdown. Overall is the mean of the seven section
 * fractions. {@code missingFields} uses dot paths (e.g.
 * {@code address.district}, {@code economic.annualIncome}) so the UI and the
 * future eligibility engine can prompt for exactly what is absent.
 */
public class ProfileCompleteness {

    private int overallPercent;
    private Map<String, Integer> sectionPercents;
    private List<String> missingFields;

    public ProfileCompleteness() {
    }

    public ProfileCompleteness(int overallPercent, Map<String, Integer> sectionPercents,
                               List<String> missingFields) {
        this.overallPercent = overallPercent;
        this.sectionPercents = sectionPercents;
        this.missingFields = missingFields;
    }

    public int getOverallPercent() { return overallPercent; }
    public void setOverallPercent(int overallPercent) { this.overallPercent = overallPercent; }

    public Map<String, Integer> getSectionPercents() { return sectionPercents; }
    public void setSectionPercents(Map<String, Integer> sectionPercents) { this.sectionPercents = sectionPercents; }

    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }
}
