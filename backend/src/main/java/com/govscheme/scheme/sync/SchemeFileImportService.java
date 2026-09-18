package com.govscheme.scheme.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.scheme.entity.SyncError;
import com.govscheme.scheme.entity.SyncErrorRepository;
import com.govscheme.matching.service.MatchingService;
import com.govscheme.scheme.entity.SyncJob;
import com.govscheme.scheme.entity.SyncJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports schemes from a local JSON dump (default {@code data/schemes.json}).
 * Each item is upserted by slug through {@link SchemeUpsertService}, so file
 * imports and API syncs share idempotency, raw preservation and job history.
 * A failed item is recorded and skipped; the rest of the file still imports.
 */
@Service
public class SchemeFileImportService {

    private static final Logger log = LoggerFactory.getLogger(SchemeFileImportService.class);

    private final SchemeUpsertService upsertService;
    private final MatchingService matchingService;
    private final SyncJobRepository jobRepository;
    private final SyncErrorRepository errorRepository;
    private final ObjectMapper objectMapper;
    private final Path importFile;

    public SchemeFileImportService(SchemeUpsertService upsertService,
                                   MatchingService matchingService,
                                   SyncJobRepository jobRepository,
                                   SyncErrorRepository errorRepository,
                                   ObjectMapper objectMapper,
                                   @Value("${app.import.file:data/schemes.json}") String importFile) {
        this.upsertService = upsertService;
        this.matchingService = matchingService;
        this.jobRepository = jobRepository;
        this.errorRepository = errorRepository;
        this.objectMapper = objectMapper;
        this.importFile = Path.of(importFile);
    }

    public SyncJob importFile() {
        SyncJob job = new SyncJob();
        job.setFromOffset(0);
        job = jobRepository.save(job);
        log.info("SCHEME_FILE_IMPORT_STARTED jobId={} file={}", job.getId(), importFile);

        List<JsonNode> items;
        try {
            String content = Files.readString(importFile);
            JsonNode root = objectMapper.readTree(content);
            if (!root.isArray()) {
                return fail(job, "Import file must contain a JSON array, found: " + root.getNodeType());
            }
            items = new ArrayList<>();
            root.forEach(items::add);
        } catch (Exception e) {
            return fail(job, "Cannot read import file: " + message(e));
        }

        job.setRequestedLimit(items.size());
        int created = 0;
        int updated = 0;
        int failed = 0;
        List<SyncError> errors = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            JsonNode item = items.get(i);
            String slug = item.has("slug") && item.get("slug").isTextual()
                ? item.get("slug").asText() : ("index-" + i);
            try {
                SchemeUpsertService.Outcome outcome = upsertService.upsertFromFile(item);
                if (outcome == SchemeUpsertService.Outcome.CREATED) {
                    created++;
                } else {
                    updated++;
                }
            } catch (Exception e) {
                failed++;
                SyncError error = new SyncError();
                error.setJobId(job.getId());
                error.setSlug(slug);
                error.setStage("FILE");
                error.setMessage(truncate(message(e)));
                errors.add(error);
                log.warn("SCHEME_FILE_IMPORT_FAILED jobId={} slug={} message={}",
                    job.getId(), slug, message(e));
            }
        }
        if (!errors.isEmpty()) {
            errorRepository.saveAll(errors);
        }

        job.setFetched(items.size());
        job.setCreatedCount(created);
        job.setUpdatedCount(updated);
        job.setFailedCount(failed);
        job.setFinishedAt(Instant.now());
        if (failed == 0) {
            job.setStatus(SyncJob.Status.SUCCESS);
        } else if (created + updated > 0) {
            job.setStatus(SyncJob.Status.PARTIAL);
        } else {
            job.setStatus(SyncJob.Status.FAILED);
        }
        job = jobRepository.save(job);
        log.info("SCHEME_FILE_IMPORT_COMPLETED jobId={} status={} fetched={} created={} updated={} failed={}",
            job.getId(), job.getStatus(), items.size(), created, updated, failed);
        try {
            matchingService.recalculateSchemesSyncedSince(job.getStartedAt());
        } catch (Exception e) {
            log.warn("MATCHING_RECALC_FAILED jobId={} message={}", job.getId(), e.getMessage());
        }
        return job;
    }

    private SyncJob fail(SyncJob job, String message) {
        job.setStatus(SyncJob.Status.FAILED);
        job.setErrorMessage(truncate(message));
        job.setFinishedAt(Instant.now());
        job = jobRepository.save(job);
        log.warn("SCHEME_FILE_IMPORT_FAILED jobId={} message={}", job.getId(), message);
        return job;
    }

    private String message(Throwable e) {
        String message = e.getMessage();
        return message == null ? e.getClass().getSimpleName() : message;
    }

    private String truncate(String message) {
        if (message == null) {
            return "Unknown error";
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
