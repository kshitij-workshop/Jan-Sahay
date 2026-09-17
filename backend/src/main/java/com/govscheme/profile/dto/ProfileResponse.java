package com.govscheme.profile.dto;

import com.govscheme.profile.entity.CasteCategory;
import com.govscheme.profile.entity.EducationLevel;
import com.govscheme.profile.entity.EmploymentStatus;
import com.govscheme.profile.entity.Gender;
import com.govscheme.profile.entity.IncomeCategory;
import com.govscheme.profile.entity.MaritalStatus;
import com.govscheme.profile.entity.Occupation;
import com.govscheme.profile.entity.UserProfile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ProfileResponse {

    private String userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String nationality;
    private EducationLevel educationLevel;
    private String institution;
    private Boolean studentStatus;
    private String course;
    private Long annualIncome;
    private IncomeCategory incomeCategory;
    private Boolean bplStatus;
    private Boolean economicDistress;
    private CasteCategory casteCategory;
    private Boolean minorityStatus;
    private Boolean disabilityStatus;
    private Integer disabilityPercentage;
    private Occupation occupation;
    private EmploymentStatus employmentStatus;
    private Boolean govtEmployee;
    private MaritalStatus maritalStatus;
    private Integer dependents;
    private Map<String, String> additionalAttributes;
    private List<AddressResponse> addresses;
    private ProfileCompleteness completeness;

    public static ProfileResponse from(UserProfile profile, List<AddressResponse> addresses,
                                       ProfileCompleteness completeness) {
        ProfileResponse dto = new ProfileResponse();
        dto.setUserId(profile.getUserId());
        dto.setFullName(profile.getFullName());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setGender(profile.getGender());
        dto.setNationality(profile.getNationality());
        dto.setEducationLevel(profile.getEducationLevel());
        dto.setInstitution(profile.getInstitution());
        dto.setStudentStatus(profile.getStudentStatus());
        dto.setCourse(profile.getCourse());
        dto.setAnnualIncome(profile.getAnnualIncome());
        dto.setIncomeCategory(profile.getIncomeCategory());
        dto.setBplStatus(profile.getBplStatus());
        dto.setEconomicDistress(profile.getEconomicDistress());
        dto.setCasteCategory(profile.getCasteCategory());
        dto.setMinorityStatus(profile.getMinorityStatus());
        dto.setDisabilityStatus(profile.getDisabilityStatus());
        dto.setDisabilityPercentage(profile.getDisabilityPercentage());
        dto.setOccupation(profile.getOccupation());
        dto.setEmploymentStatus(profile.getEmploymentStatus());
        dto.setGovtEmployee(profile.getGovtEmployee());
        dto.setMaritalStatus(profile.getMaritalStatus());
        dto.setDependents(profile.getDependents());
        dto.setAdditionalAttributes(profile.getAdditionalAttributes());
        dto.setAddresses(addresses);
        dto.setCompleteness(completeness);
        return dto;
    }

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

    public List<AddressResponse> getAddresses() { return addresses; }
    public void setAddresses(List<AddressResponse> addresses) { this.addresses = addresses; }

    public ProfileCompleteness getCompleteness() { return completeness; }
    public void setCompleteness(ProfileCompleteness completeness) { this.completeness = completeness; }
}
