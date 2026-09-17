package com.govscheme.scheme.dto;

import com.govscheme.scheme.entity.Scheme;

public class SchemeSummaryResponse {

    private String id;
    private String slug;
    private String name;
    private String nameEng;
    private String shortTitle;
    private String category;
    private String state;
    private String level;
    private String benefitType;
    private String description;

    public static SchemeSummaryResponse from(Scheme scheme) {
        SchemeSummaryResponse dto = new SchemeSummaryResponse();
        dto.setId(scheme.getId());
        dto.setSlug(scheme.getSlug());
        dto.setName(scheme.getSchemeName());
        dto.setNameEng(scheme.getSchemeNameEng());
        dto.setShortTitle(scheme.getShortTitle());
        dto.setCategory(scheme.getSchemeCategory());
        dto.setState(scheme.getBeneficiaryState());
        dto.setLevel(scheme.getLevel());
        dto.setBenefitType(scheme.getBenefitType());
        dto.setDescription(scheme.getBriefDescriptionEng() != null
            ? scheme.getBriefDescriptionEng() : scheme.getBriefDescription());
        return dto;
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

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getBenefitType() { return benefitType; }
    public void setBenefitType(String benefitType) { this.benefitType = benefitType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
