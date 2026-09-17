package com.govscheme.scheme.sync;

import com.govscheme.scheme.client.MySchemeClient;
import com.govscheme.scheme.client.MySchemeException;
import com.govscheme.scheme.client.MySchemeProperties;
import com.govscheme.scheme.entity.SyncError;
import com.govscheme.scheme.entity.SyncErrorRepository;
import com.govscheme.matching.service.MatchingService;
import com.govscheme.scheme.entity.SyncJob;
import com.govscheme.scheme.entity.SyncJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Pulls scheme slugs from paginated search, then fetches detail/FAQs/documents
 * per scheme with bounded parallelism. Upserts are idempotent by slug, so a
 * failed run resumes simply by re-running from the same offset.
 */
@Service
public class SchemeSyncService {

    private static final Logger log = LoggerFactory.getLogger(SchemeSyncService.class);
    private static final int SEARCH_PAGE_SIZE = 50;

    private final MySchemeClient client;
    private final MySchemeProperties properties;
    private final SchemeUpsertService upsertService;
    private final MatchingService matchingService;
    private final SyncJobRepository jobRepository;
    private final SyncErrorRepository errorRepository;

    public SchemeSyncService(MySchemeClient client,
                             MySchemeProperties properties,
                             SchemeUpsertService upsertService,
                             MatchingService matchingService,
                             SyncJobRepository jobRepository,
                             SyncErrorRepository errorRepository) {
        this.client = client;
        this.properties = properties;
        this.upsertService = upsertService;
        this.matchingService = matchingService;
        this.jobRepository = jobRepository;
        this.errorRepository = errorRepository;
    }

    public SyncJob runSync(String lang, int from, Integer limit) {
        int effectiveLimit = Math.min(
            limit == null ? properties.getMaxSyncSchemes() : limit,
            properties.getMaxSyncSchemes());
        int start = Math.max(0, from);

        SyncJob job = new SyncJob();
        job.setFromOffset(start);
        job.setRequestedLimit(effectiveLimit);
        job = jobRepository.save(job);
        log.info("SCHEME_SYNC_STARTED jobId={} from={} limit={} lang={}", job.getId(), start, effectiveLimit, lang);

        List<MySchemeClient.SchemeSummary> slugs = new ArrayList<>();
        try {
            int offset = start;
            while (slugs.size() < effectiveLimit) {
                int size = Math.min(SEARCH_PAGE_SIZE, effectiveLimit - slugs.size());
                MySchemeClient.SchemeSearchPage page = client.searchSchemes(lang, offset, size);
                if (page.items().isEmpty()) {
                    break;
                }
                slugs.addAll(page.items());
                offset += page.items().size();
                if (page.items().size() < size) {
                    break;
                }
                if (page.total() > 0 && offset - start >= page.total()) {
                    break;
                }
            }
        } catch (MySchemeException e) {
            return fail(job, "Search failed: " + truncate(e.getMessage()));
        }

        int concurrency = Math.max(1, properties.getDetailConcurrency());
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        List<PerSchemeOutcome> outcomes = new ArrayList<>();
        try {
            List<Future<PerSchemeOutcome>> futures = new ArrayList<>();
            for (MySchemeClient.SchemeSummary summary : slugs) {
                futures.add(pool.submit(task(lang, summary)));
            }
            for (Future<PerSchemeOutcome> future : futures) {
                try {
                    outcomes.add(future.get());
                } catch (Exception e) {
                    outcomes.add(PerSchemeOutcome.failed(null, "DETAIL",
                        truncate(rootMessage(e))));
                }
            }
        } finally {
            pool.shutdown();
        }

        int created = 0;
        int updated = 0;
        int failed = 0;
        List<SyncError> errors = new ArrayList<>();
        for (PerSchemeOutcome outcome : outcomes) {
            switch (outcome.result()) {
                case CREATED -> created++;
                case UPDATED -> updated++;
                case FAILED -> {
                    failed++;
                    SyncError error = new SyncError();
                    error.setJobId(job.getId());
                    error.setSlug(outcome.slug());
                    error.setStage(outcome.stage());
                    error.setMessage(outcome.message());
                    errors.add(error);
                    log.warn("SCHEME_SYNC_FAILED jobId={} slug={} stage={} message={}",
                        job.getId(), outcome.slug(), outcome.stage(), outcome.message());
                }
            }
        }
        if (!errors.isEmpty()) {
            errorRepository.saveAll(errors);
        }

        job.setFetched(slugs.size());
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
        log.info("SCHEME_SYNC_COMPLETED jobId={} status={} fetched={} created={} updated={} failed={}",
            job.getId(), job.getStatus(), job.getFetched(), created, updated, failed);
        try {
            matchingService.recalculateSchemesSyncedSince(job.getStartedAt());
        } catch (Exception e) {
            log.warn("MATCHING_RECALC_FAILED jobId={} message={}", job.getId(), e.getMessage());
        }
        return job;
    }

    private Callable<PerSchemeOutcome> task(String lang, MySchemeClient.SchemeSummary summary) {
        return () -> {
            try {
                String detail = client.fetchSchemeDetailJson(summary.slug(), lang);
                String schemeId = summary.schemeId();
                String faqs = null;
                String documents = null;
                if (schemeId != null && !schemeId.isBlank()) {
                    try {
                        faqs = client.fetchSchemeFaqsJson(schemeId, lang);
                    } catch (MySchemeException e) {
                        log.warn("SCHEME_SYNC_FAQS_SKIPPED slug={} message={}", summary.slug(), e.getMessage());
                    }
                    try {
                        documents = client.fetchSchemeDocumentsJson(schemeId, lang);
                    } catch (MySchemeException e) {
                        log.warn("SCHEME_SYNC_DOCUMENTS_SKIPPED slug={} message={}", summary.slug(), e.getMessage());
                    }
                }
                SchemeUpsertService.Outcome outcome =
                    upsertService.upsert(summary.slug(), detail, faqs, documents, lang);
                return outcome == SchemeUpsertService.Outcome.CREATED
                    ? PerSchemeOutcome.created(summary.slug())
                    : PerSchemeOutcome.updated(summary.slug());
            } catch (MySchemeException e) {
                return PerSchemeOutcome.failed(summary.slug(), "DETAIL", truncate(e.getMessage()));
            } catch (Exception e) {
                return PerSchemeOutcome.failed(summary.slug(), "NORMALIZE", truncate(rootMessage(e)));
            }
        };
    }

    private SyncJob fail(SyncJob job, String message) {
        job.setStatus(SyncJob.Status.FAILED);
        job.setErrorMessage(truncate(message));
        job.setFinishedAt(Instant.now());
        job = jobRepository.save(job);
        log.warn("SCHEME_SYNC_FAILED jobId={} message={}", job.getId(), message);
        return job;
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage();
        return message == null ? e.getClass().getSimpleName() : message;
    }

    private String truncate(String message) {
        if (message == null) {
            return "Unknown error";
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    enum Result {
        CREATED, UPDATED, FAILED
    }

    record PerSchemeOutcome(Result result, String slug, String stage, String message) {

        static PerSchemeOutcome created(String slug) {
            return new PerSchemeOutcome(Result.CREATED, slug, null, null);
        }

        static PerSchemeOutcome updated(String slug) {
            return new PerSchemeOutcome(Result.UPDATED, slug, null, null);
        }

        static PerSchemeOutcome failed(String slug, String stage, String message) {
            return new PerSchemeOutcome(Result.FAILED, slug, stage, message);
        }
    }
}
