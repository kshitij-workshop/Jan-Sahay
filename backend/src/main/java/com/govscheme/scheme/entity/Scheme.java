package com.govscheme.scheme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "schemes")
public class Scheme {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    @Column(name = "scheme_name", length = 500)
    private String schemeName;

    @Column(name = "scheme_name_eng", length = 500)
    private String schemeNameEng;

    @Column(name = "short_title", length = 255)
    private String shortTitle;

    @Column(name = "scheme_category", length = 255)
    private String schemeCategory;

    @Column(name = "beneficiary_state", length = 255)
    private String beneficiaryState;

    @Column(name = "level", length = 50)
    private String level;

    @Column(name = "scheme_for", length = 255)
    private String schemeFor;

    @Column(name = "nodal_ministry", length = 500)
    private String nodalMinistry;

    @Column(name = "brief_description", columnDefinition = "TEXT")
    private String briefDescription;

    @Column(name = "brief_description_eng", columnDefinition = "TEXT")
    private String briefDescriptionEng;

    @Column(name = "close_date", length = 50)
    private String closeDate;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "source", nullable = false, length = 50)
    private String source = "MYSCHEME";

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }

    public String getSchemeNameEng() { return schemeNameEng; }
    public void setSchemeNameEng(String schemeNameEng) { this.schemeNameEng = schemeNameEng; }

    public String getShortTitle() { return shortTitle; }
    public void setShortTitle(String shortTitle) { this.shortTitle = shortTitle; }

    public String getSchemeCategory() { return schemeCategory; }
    public void setSchemeCategory(String schemeCategory) { this.schemeCategory = schemeCategory; }

    public String getBeneficiaryState() { return beneficiaryState; }
    public void setBeneficiaryState(String beneficiaryState) { this.beneficiaryState = beneficiaryState; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getSchemeFor() { return schemeFor; }
    public void setSchemeFor(String schemeFor) { this.schemeFor = schemeFor; }

    public String getNodalMinistry() { return nodalMinistry; }
    public void setNodalMinistry(String nodalMinistry) { this.nodalMinistry = nodalMinistry; }

    public String getBriefDescription() { return briefDescription; }
    public void setBriefDescription(String briefDescription) { this.briefDescription = briefDescription; }

    public String getBriefDescriptionEng() { return briefDescriptionEng; }
    public void setBriefDescriptionEng(String briefDescriptionEng) { this.briefDescriptionEng = briefDescriptionEng; }

    public String getCloseDate() { return closeDate; }
    public void setCloseDate(String closeDate) { this.closeDate = closeDate; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Instant lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
