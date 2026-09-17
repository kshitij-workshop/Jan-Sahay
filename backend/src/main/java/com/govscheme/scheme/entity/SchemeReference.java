package com.govscheme.scheme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "scheme_references")
public class SchemeReference {

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

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "position", nullable = false)
    private int position;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
