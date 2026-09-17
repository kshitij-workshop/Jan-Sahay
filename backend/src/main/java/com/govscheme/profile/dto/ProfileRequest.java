package com.govscheme.profile.dto;

import com.govscheme.profile.entity.AddressType;
import com.govscheme.profile.entity.AreaType;
import com.govscheme.profile.entity.CasteCategory;
import com.govscheme.profile.entity.EducationLevel;
import com.govscheme.profile.entity.EmploymentStatus;
import com.govscheme.profile.entity.Gender;
import com.govscheme.profile.entity.IncomeCategory;
import com.govscheme.profile.entity.MaritalStatus;
import com.govscheme.profile.entity.Occupation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Partial-update DTO: every field is optional. {@code null} means "leave the
 * stored value untouched". Malformed values yield 400; merely absent values
 * are reported via completeness {@code missingFields}, never as errors.
 */
public class ProfileRequest {

    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 100, message = "Nationality must be at most 100 characters")
    private String nationality;

    private EducationLevel educationLevel;

    @Size(max = 255, message = "Institution must be at most 255 characters")
    private String institution;

    private Boolean studentStatus;

    @Size(max = 255, message = "Course must be at most 255 characters")
    private String course;

    @PositiveOrZero(message = "Annual income must be zero or positive")
    @Max(value = 100000000, message = "Annual income looks unrealistic")
    private Long annualIncome;

    private IncomeCategory incomeCategory;

    private Boolean bplStatus;

    private Boolean economicDistress;

    private CasteCategory casteCategory;

    private Boolean minorityStatus;

    private Boolean disabilityStatus;

    @Min(value = 0, message = "Disability percentage must be between 0 and 100")
    @Max(value = 100, message = "Disability percentage must be between 0 and 100")
    private Integer disabilityPercentage;

    private Occupation occupation;

    private EmploymentStatus employmentStatus;

    private Boolean govtEmployee;

    private MaritalStatus maritalStatus;

    @Min(value = 0, message = "Dependents must be zero or positive")
    @Max(value = 30, message = "Dependents looks unrealistic")
    private Integer dependents;

    @Valid
    private List<AddressRequest> addresses;

    @Size(max = 50, message = "At most 50 additional attributes are allowed")
    private Map<@Size(max = 100) String, @Size(max = 500) String> additionalAttributes;

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

    public List<AddressRequest> getAddresses() { return addresses; }
    public void setAddresses(List<AddressRequest> addresses) { this.addresses = addresses; }

    public Map<String, String> getAdditionalAttributes() { return additionalAttributes; }
    public void setAdditionalAttributes(Map<String, String> additionalAttributes) { this.additionalAttributes = additionalAttributes; }
}
