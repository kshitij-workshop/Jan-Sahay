package com.govscheme.scheme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "scheme_categories")
public class SchemeCategory {

    public static final String KIND_MAIN = "MAIN";
    public static final String KIND_SUB = "SUB";

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    @Column(name = "scheme_id", nullable = false, columnDefinition = "CHAR(36)")
    private String schemeId;

    @Column(name = "category", nullable = false, length = 255)
    private String category;

    @Column(name = "kind", nullable = false, length = 10)
    private String kind = KIND_MAIN;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
}
