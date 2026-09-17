package com.govscheme.scheme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "scheme_states")
public class SchemeState {

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

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
