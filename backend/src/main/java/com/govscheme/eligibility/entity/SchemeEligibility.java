package com.govscheme.eligibility.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Admin-curated, machine-readable eligibility criteria for one scheme.
 * Every field is nullable: {@code null} means the scheme does not constrain
 * that dimension and the matching rule is skipped entirely. Free-text
 * {@code eligibility_md} is display-only and never evaluated.
 */
@Entity
@Table(name = "scheme_eligibility")
public class SchemeEligibility {

    @Id
    @Column(name = "scheme_id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String schemeId;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "states", columnDefinition = "TEXT")
    private String states;

    @Column(name = "genders", length = 100)
    private String genders;

    @Column(name = "max_annual_income")
    private Long maxAnnualIncome;

    @Column(name = "caste_categories", length = 255)
    private String casteCategories;

    @Column(name = "occupations", length = 500)
    private String occupations;

    @Column(name = "require_student")
    private Boolean requireStudent;

    @Column(name = "require_disabled")
    private Boolean requireDisabled;

    @Column(name = "min_disability_percentage")
    private Integer minDisabilityPercentage;

    @Column(name = "area_types", length = 50)
    private String areaTypes;

    @Column(name = "employment_statuses", length = 255)
    private String employmentStatuses;

    @Column(name = "require_govt_employee")
    private Boolean requireGovtEmployee;

    @Column(name = "require_bpl")
    private Boolean requireBpl;

    @Column(name = "marital_statuses", length = 255)
    private String maritalStatuses;

    @Column(name = "require_minority")
    private Boolean requireMinority;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }

    public Integer getMaxAge() { return maxAge; }
    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }

    public String getStates() { return states; }
    public void setStates(String states) { this.states = states; }

    public String getGenders() { return genders; }
    public void setGenders(String genders) { this.genders = genders; }

    public Long getMaxAnnualIncome() { return maxAnnualIncome; }
    public void setMaxAnnualIncome(Long maxAnnualIncome) { this.maxAnnualIncome = maxAnnualIncome; }

    public String getCasteCategories() { return casteCategories; }
    public void setCasteCategories(String casteCategories) { this.casteCategories = casteCategories; }

    public String getOccupations() { return occupations; }
    public void setOccupations(String occupations) { this.occupations = occupations; }

    public Boolean getRequireStudent() { return requireStudent; }
    public void setRequireStudent(Boolean requireStudent) { this.requireStudent = requireStudent; }

    public Boolean getRequireDisabled() { return requireDisabled; }
    public void setRequireDisabled(Boolean requireDisabled) { this.requireDisabled = requireDisabled; }

    public Integer getMinDisabilityPercentage() { return minDisabilityPercentage; }
    public void setMinDisabilityPercentage(Integer minDisabilityPercentage) { this.minDisabilityPercentage = minDisabilityPercentage; }

    public String getAreaTypes() { return areaTypes; }
    public void setAreaTypes(String areaTypes) { this.areaTypes = areaTypes; }

    public String getEmploymentStatuses() { return employmentStatuses; }
    public void setEmploymentStatuses(String employmentStatuses) { this.employmentStatuses = employmentStatuses; }

    public Boolean getRequireGovtEmployee() { return requireGovtEmployee; }
    public void setRequireGovtEmployee(Boolean requireGovtEmployee) { this.requireGovtEmployee = requireGovtEmployee; }

    public Boolean getRequireBpl() { return requireBpl; }
    public void setRequireBpl(Boolean requireBpl) { this.requireBpl = requireBpl; }

    public String getMaritalStatuses() { return maritalStatuses; }
    public void setMaritalStatuses(String maritalStatuses) { this.maritalStatuses = maritalStatuses; }

    public Boolean getRequireMinority() { return requireMinority; }
    public void setRequireMinority(Boolean requireMinority) { this.requireMinority = requireMinority; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
