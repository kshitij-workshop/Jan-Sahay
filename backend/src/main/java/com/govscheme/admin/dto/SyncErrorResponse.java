package com.govscheme.admin.dto;

import com.govscheme.scheme.entity.SyncError;

import java.time.Instant;

public class SyncErrorResponse {

    private String id;
    private String jobId;
    private String slug;
    private String stage;
    private String message;
    private Instant createdAt;

    public static SyncErrorResponse from(SyncError error) {
        SyncErrorResponse dto = new SyncErrorResponse();
        dto.setId(error.getId());
        dto.setJobId(error.getJobId());
        dto.setSlug(error.getSlug());
        dto.setStage(error.getStage());
        dto.setMessage(error.getMessage());
        dto.setCreatedAt(error.getCreatedAt());
        return dto;
    }

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
