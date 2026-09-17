package com.govscheme.admin.dto;

import com.govscheme.scheme.entity.SyncJob;

import java.time.Instant;

public class SyncJobResponse {

    private String id;
    private String status;
    private int fromOffset;
    private int requestedLimit;
    private int fetched;
    private int createdCount;
    private int updatedCount;
    private int failedCount;
    private String errorMessage;
    private Instant startedAt;
    private Instant finishedAt;

    public static SyncJobResponse from(SyncJob job) {
        SyncJobResponse dto = new SyncJobResponse();
        dto.setId(job.getId());
        dto.setStatus(job.getStatus().name());
        dto.setFromOffset(job.getFromOffset());
        dto.setRequestedLimit(job.getRequestedLimit());
        dto.setFetched(job.getFetched());
        dto.setCreatedCount(job.getCreatedCount());
        dto.setUpdatedCount(job.getUpdatedCount());
        dto.setFailedCount(job.getFailedCount());
        dto.setErrorMessage(job.getErrorMessage());
        dto.setStartedAt(job.getStartedAt());
        dto.setFinishedAt(job.getFinishedAt());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFromOffset() { return fromOffset; }
    public void setFromOffset(int fromOffset) { this.fromOffset = fromOffset; }

    public int getRequestedLimit() { return requestedLimit; }
    public void setRequestedLimit(int requestedLimit) { this.requestedLimit = requestedLimit; }

    public int getFetched() { return fetched; }
    public void setFetched(int fetched) { this.fetched = fetched; }

    public int getCreatedCount() { return createdCount; }
    public void setCreatedCount(int createdCount) { this.createdCount = createdCount; }

    public int getUpdatedCount() { return updatedCount; }
    public void setUpdatedCount(int updatedCount) { this.updatedCount = updatedCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
}
