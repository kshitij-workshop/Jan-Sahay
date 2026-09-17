package com.govscheme.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Citizen profile. One row per user (shared primary key with {@code users.id}).
 * <p>
 * Wrapper types (not primitives) are used deliberately: {@code null} means
 * "not provided", which the eligibility engine maps to INSUFFICIENT_INFORMATION
 * instead of NOT_ELIGIBLE. New scheme criteria that do not fit the columns go
 * into {@code additionalAttributes} so the schema never needs rewriting.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "nationality", length = 100)
    private String nationality = "Indian";

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 40)
    private EducationLevel educationLevel;

    @Column(name = "institution", length = 255)
    private String institution;

    @Column(name = "student_status")
    private Boolean studentStatus;

    @Column(name = "course", length = 255)
    private String course;

    @Column(name = "annual_income")
    private Long annualIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_category", length = 40)
    private IncomeCategory incomeCategory;

    @Column(name = "bpl_status")
    private Boolean bplStatus;

    @Column(name = "economic_distress")
    private Boolean economicDistress;

    @Enumerated(EnumType.STRING)
    @Column(name = "caste_category", length = 20)
    private CasteCategory casteCategory;

    @Column(name = "minority_status")
    private Boolean minorityStatus;

    @Column(name = "disability_status")
    private Boolean disabilityStatus;

    @Column(name = "disability_percentage")
    private Integer disabilityPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupation", length = 40)
    private Occupation occupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 40)
    private EmploymentStatus employmentStatus;

    @Column(name = "govt_employee")
    private Boolean govtEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "dependents")
    private Integer dependents;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_attributes", columnDefinition = "JSON")
    private Map<String, String> additionalAttributes = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public EducationLevel getEducationLevel() { return educationLevel; }
    public void setEducationLevel(EducationLevel educationLevel) { this.educationLevel = educationLevel; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public Boolean getStudentStatus() { return studentStatus; }
    public void setStudentStatus(Boolean studentStatus) { this.studentStatus = studentStatus; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public Long getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(Long annualIncome) { this.annualIncome = annualIncome; }

    public IncomeCategory getIncomeCategory() { return incomeCategory; }
    public void setIncomeCategory(IncomeCategory incomeCategory) { this.incomeCategory = incomeCategory; }

    public Boolean getBplStatus() { return bplStatus; }
    public void setBplStatus(Boolean bplStatus) { this.bplStatus = bplStatus; }

    public Boolean getEconomicDistress() { return economicDistress; }
    public void setEconomicDistress(Boolean economicDistress) { this.economicDistress = economicDistress; }

    public CasteCategory getCasteCategory() { return casteCategory; }
    public void setCasteCategory(CasteCategory casteCategory) { this.casteCategory = casteCategory; }

    public Boolean getMinorityStatus() { return minorityStatus; }
    public void setMinorityStatus(Boolean minorityStatus) { this.minorityStatus = minorityStatus; }

    public Boolean getDisabilityStatus() { return disabilityStatus; }
    public void setDisabilityStatus(Boolean disabilityStatus) { this.disabilityStatus = disabilityStatus; }

    public Integer getDisabilityPercentage() { return disabilityPercentage; }
    public void setDisabilityPercentage(Integer disabilityPercentage) { this.disabilityPercentage = disabilityPercentage; }

    public Occupation getOccupation() { return occupation; }
    public void setOccupation(Occupation occupation) { this.occupation = occupation; }

    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(EmploymentStatus employmentStatus) { this.employmentStatus = employmentStatus; }

    public Boolean getGovtEmployee() { return govtEmployee; }
    public void setGovtEmployee(Boolean govtEmployee) { this.govtEmployee = govtEmployee; }

    public MaritalStatus getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(MaritalStatus maritalStatus) { this.maritalStatus = maritalStatus; }

    public Integer getDependents() { return dependents; }
    public void setDependents(Integer dependents) { this.dependents = dependents; }

    public Map<String, String> getAdditionalAttributes() { return additionalAttributes; }
    public void setAdditionalAttributes(Map<String, String> additionalAttributes) { this.additionalAttributes = additionalAttributes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
