package com.govscheme.scheme.dto;

import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeApplicationStep;
import com.govscheme.scheme.entity.SchemeBeneficiary;
import com.govscheme.scheme.entity.SchemeCategory;
import com.govscheme.scheme.entity.SchemeDocument;
import com.govscheme.scheme.entity.SchemeFaq;
import com.govscheme.scheme.entity.SchemeReference;
import com.govscheme.scheme.entity.SchemeState;
import com.govscheme.scheme.entity.SchemeTag;

import java.time.Instant;
import java.util.List;

/**
 * Full scheme detail for the Scheme details page. Eligibility matching,
 * document readiness and AI explanations arrive in later phases; this DTO
 * carries only stored facts plus sync provenance.
 */
public class SchemeDetailResponse {

    private String id;
    private String slug;
    private String name;
    private String nameEng;
    private String shortTitle;
    private String category;
    private List<Category> categories;
    private String state;
    private List<String> states;
    private String level;
    private String schemeFor;
    private List<String> beneficiaries;
    private String ministry;
    private String department;
    private String description;
    private String detailedDescription;
    private String benefits;
    private String eligibility;
    private String exclusions;
    private String documentsText;
    private String benefitType;
    private String schemeType;
    private List<String> tags;
    private List<Faq> faqs;
    private List<Document> documents;
    private List<Step> applicationProcess;
    private List<Reference> references;
    private String source;
    private String sourceUrl;
    private Instant lastSyncedAt;

    public static SchemeDetailResponse from(Scheme scheme,
                                            List<SchemeTag> tags,
                                            List<SchemeState> states,
                                            List<SchemeCategory> categories,
                                            List<SchemeBeneficiary> beneficiaries,
                                            List<SchemeFaq> faqs,
                                            List<SchemeDocument> documents,
                                            List<SchemeApplicationStep> steps,
                                            List<SchemeReference> references) {
        SchemeDetailResponse dto = new SchemeDetailResponse();
        dto.setId(scheme.getId());
        dto.setSlug(scheme.getSlug());
        dto.setName(scheme.getSchemeName());
        dto.setNameEng(scheme.getSchemeNameEng());
        dto.setShortTitle(scheme.getShortTitle());
        dto.setCategory(scheme.getSchemeCategory());
        dto.setCategories(categories.stream()
            .map(c -> new Category(c.getCategory(), c.getKind()))
            .toList());
        dto.setState(scheme.getBeneficiaryState());
        dto.setStates(states.stream().map(SchemeState::getState).toList());
        dto.setLevel(scheme.getLevel());
        dto.setSchemeFor(scheme.getSchemeFor());
        dto.setBeneficiaries(beneficiaries.stream().map(SchemeBeneficiary::getBeneficiary).toList());
        dto.setMinistry(scheme.getNodalMinistry());
        dto.setDepartment(scheme.getDepartment());
        dto.setDescription(scheme.getBriefDescriptionEng() != null
            ? scheme.getBriefDescriptionEng() : scheme.getBriefDescription());
        dto.setDetailedDescription(scheme.getDetailedDescriptionMd());
        dto.setBenefits(scheme.getBenefitsMd());
        dto.setEligibility(scheme.getEligibilityMd());
        dto.setExclusions(scheme.getExclusionsMd());
        dto.setDocumentsText(scheme.getDocumentsMd());
        dto.setBenefitType(scheme.getBenefitType());
        dto.setSchemeType(scheme.getSchemeType());
        dto.setTags(tags.stream().map(SchemeTag::getTag).toList());
        dto.setFaqs(faqs.stream()
            .map(f -> new Faq(f.getQuestion(), f.getAnswer()))
            .toList());
        dto.setDocuments(documents.stream()
            .map(d -> new Document(d.getName(), d.getRequired()))
            .toList());
        dto.setApplicationProcess(steps.stream()
            .map(s -> new Step(s.getStepNo(), s.getDescription()))
            .toList());
        dto.setReferences(references.stream()
            .map(r -> new Reference(r.getTitle(), r.getUrl()))
            .toList());
        dto.setSource(scheme.getSource());
        dto.setSourceUrl(scheme.getSourceUrl());
        dto.setLastSyncedAt(scheme.getLastSyncedAt());
        return dto;
    }

    public record Faq(String question, String answer) {
    }

    public record Document(String name, Boolean required) {
    }

    public record Step(int stepNo, String description) {
    }

    public record Reference(String title, String url) {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameEng() { return nameEng; }
    public void setNameEng(String nameEng) { this.nameEng = nameEng; }

    public String getShortTitle() { return shortTitle; }
    public void setShortTitle(String shortTitle) { this.shortTitle = shortTitle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }

    public record Category(String category, String kind) {
    }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public List<String> getStates() { return states; }
    public void setStates(List<String> states) { this.states = states; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getSchemeFor() { return schemeFor; }
    public void setSchemeFor(String schemeFor) { this.schemeFor = schemeFor; }

    public List<String> getBeneficiaries() { return beneficiaries; }
    public void setBeneficiaries(List<String> beneficiaries) { this.beneficiaries = beneficiaries; }

    public String getMinistry() { return ministry; }
    public void setMinistry(String ministry) { this.ministry = ministry; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetailedDescription() { return detailedDescription; }
    public void setDetailedDescription(String detailedDescription) { this.detailedDescription = detailedDescription; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    public String getExclusions() { return exclusions; }
    public void setExclusions(String exclusions) { this.exclusions = exclusions; }

    public String getDocumentsText() { return documentsText; }
    public void setDocumentsText(String documentsText) { this.documentsText = documentsText; }

    public String getBenefitType() { return benefitType; }
    public void setBenefitType(String benefitType) { this.benefitType = benefitType; }

    public String getSchemeType() { return schemeType; }
    public void setSchemeType(String schemeType) { this.schemeType = schemeType; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<Faq> getFaqs() { return faqs; }
    public void setFaqs(List<Faq> faqs) { this.faqs = faqs; }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }

    public List<Step> getApplicationProcess() { return applicationProcess; }
    public void setApplicationProcess(List<Step> applicationProcess) { this.applicationProcess = applicationProcess; }

    public List<Reference> getReferences() { return references; }
    public void setReferences(List<Reference> references) { this.references = references; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Instant lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}
