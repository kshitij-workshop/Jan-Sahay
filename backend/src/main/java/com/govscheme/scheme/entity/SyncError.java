package com.govscheme.scheme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_errors")
public class SyncError {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    @Column(name = "job_id", nullable = false, columnDefinition = "CHAR(36)")
    private String jobId;

    @Column(name = "slug", length = 255)
    private String slug;

    @Column(name = "stage", nullable = false, length = 50)
    private String stage;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
