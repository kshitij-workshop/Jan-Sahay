package com.govscheme.eligibility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.govscheme.eligibility.entity.SchemeEligibility;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CriteriaExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private SchemeEligibility extract(String eligibilityMd) {
        ObjectNode item = mapper.createObjectNode();
        item.put("slug", "test");
        item.put("eligibility_md", eligibilityMd);
        return CriteriaExtractor.extract(item);
    }

    @Test
    void extractsApplicantAgeRange() {
        SchemeEligibility c =
            extract("The applicant must be aged between 18 and 50 years.");

        assertThat(c.getMinAge()).isEqualTo(18);
        assertThat(c.getMaxAge()).isEqualTo(50);
    }

    @Test
    void extractsDashedAgeRange() {
        SchemeEligibility c =
            extract("The media representative's age is between 21-70 years.");

        assertThat(c.getMinAge()).isEqualTo(21);
        assertThat(c.getMaxAge()).isEqualTo(70);
    }

    @Test
    void ignoresDeceasedMemberAge() {
        SchemeEligibility c = extract(
            "The deceased earning member should be above 18 years and below 60 years of age.");

        assertThat(c.getMinAge()).isNull();
        assertThat(c.getMaxAge()).isNull();
    }

    @Test
    void seniorCitizenMapsToSixty() {
        SchemeEligibility c =
            extract("The applicant must be a Senior Citizen.");

        assertThat(c.getMinAge()).isEqualTo(60);
    }

    @Test
    void extractsSingleIncomeCap() {
        SchemeEligibility c = extract(
            "The annual family income should not exceed ₹1,50,000.");

        assertThat(c.getMaxAnnualIncome()).isEqualTo(150000L);
    }

    @Test
    void extractsLakhIncomeCap() {
        SchemeEligibility c =
            extract("Annual income of the family must be less than Rs. 2.5 lakh.");

        assertThat(c.getMaxAnnualIncome()).isEqualTo(250000L);
    }

    @Test
    void skipsAreaConditionalIncomeCaps() {
        SchemeEligibility c = extract(
            "The annual family income should not exceed ₹46,080 in rural areas. "
            + "The annual family income should not exceed ₹56,460 in urban areas.");

        assertThat(c.getMaxAnnualIncome()).isNull();
    }

    @Test
    void extractsFemaleOnlyScheme() {
        SchemeEligibility c =
            extract("The applicant must be female.");

        assertThat(c.getGenders()).isEqualTo("FEMALE");
    }

    @Test
    void skipsGenderWhenBothMentioned() {
        SchemeEligibility c = extract(
            "Applicable in case of death of the earning head (male or female).");

        assertThat(c.getGenders()).isNull();
    }

    @Test
    void skipsGenderWhenTransgenderIncluded() {
        SchemeEligibility c = extract(
            "The applicant must be female. The scheme also benefits the Transgender community.");

        assertThat(c.getGenders()).isNull();
    }

    @Test
    void extractsStudentRequirement() {
        SchemeEligibility c = extract(
            "The applicant must be a bonafide student enrolled in a recognized college.");

        assertThat(c.getRequireStudent()).isTrue();
    }

    @Test
    void ignoresInstitutionOnlyMentions() {
        SchemeEligibility c = extract(
            "Eligible entities: State Schools, Colleges under the Central Government.");

        assertThat(c.getRequireStudent()).isNull();
    }

    @Test
    void extractsCasteList() {
        SchemeEligibility c = extract(
            "Beneficiaries belonging to SC, ST and OBC categories are eligible.");

        assertThat(c.getCasteCategories()).isEqualTo("SC,ST,OBC");
    }

    @Test
    void skipsOpenCategoryText() {
        SchemeEligibility c = extract(
            "All categories including General category applicants are eligible. SC applicants preferred.");

        assertThat(c.getCasteCategories()).isNull();
    }

    @Test
    void extractsFarmerOccupation() {
        SchemeEligibility c = extract(
            "The subsidy is available for small and marginal farmers holding land.");

        assertThat(c.getOccupations()).isEqualTo("FARMER");
    }

    @Test
    void extractsResidenceStates() {
        SchemeEligibility c = extract(
            "The applicant must be a permanent resident of Uttar Pradesh. "
            + "Applicants residing and working in Bihar may also apply.");

        assertThat(c.getStates()).isEqualTo("Uttar Pradesh,Bihar");
    }

    @Test
    void extractsDomicile() {
        SchemeEligibility c =
            extract("Domicile of Jharkhand is mandatory.");

        assertThat(c.getStates()).isEqualTo("Jharkhand");
    }

    @Test
    void extractsBplRequirement() {
        SchemeEligibility c =
            extract("Families must belong to the BPL category and hold a valid card.");

        assertThat(c.getRequireBpl()).isTrue();
    }

    @Test
    void skipsBplWhenAplAlternativeExists() {
        SchemeEligibility c =
            extract("BPL or APL card holders are eligible.");

        assertThat(c.getRequireBpl()).isNull();
    }

    @Test
    void extractsDisabilityWithPercentage() {
        SchemeEligibility c = extract(
            "Scheme for persons with disabilities having 40% or above disability.");

        assertThat(c.getRequireDisabled()).isTrue();
        assertThat(c.getMinDisabilityPercentage()).isEqualTo(40);
    }

    @Test
    void extractsMinorityRequirement() {
        SchemeEligibility c = extract(
            "Applicants belonging to minority communities are eligible.");

        assertThat(c.getRequireMinority()).isTrue();
    }

    @Test
    void emptyTextYieldsNoCriteria() {
        ObjectNode item = mapper.createObjectNode();
        item.put("slug", "empty");

        SchemeEligibility c = CriteriaExtractor.extract(item);

        assertThat(c.getMinAge()).isNull();
        assertThat(c.getStates()).isNull();
        assertThat(c.getMaxAnnualIncome()).isNull();
        assertThat(c.getGenders()).isNull();
        assertThat(c.getRequireStudent()).isNull();
        assertThat(c.getCasteCategories()).isNull();
        assertThat(c.getOccupations()).isNull();
        assertThat(c.getRequireBpl()).isNull();
        assertThat(c.getRequireDisabled()).isNull();
        assertThat(c.getRequireMinority()).isNull();
    }
}
