package com.govscheme.scheme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "scheme_documents")
public class SchemeDocument {

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

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "required", nullable = false)
    private Boolean required = true;

    @Column(name = "language", nullable = false, length = 10)
    private String language = "en";

    @Column(name = "position", nullable = false)
    private int position;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
