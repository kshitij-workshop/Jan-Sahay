package com.govscheme.admin.controller;

import com.govscheme.admin.dto.SyncErrorResponse;
import com.govscheme.admin.dto.SyncJobResponse;
import com.govscheme.common.dto.ApiResponse;
import com.govscheme.common.dto.PageResponse;
import com.govscheme.eligibility.service.CriteriaBootstrapService;
import com.govscheme.scheme.entity.SyncErrorRepository;
import com.govscheme.scheme.entity.SyncJob;
import com.govscheme.scheme.entity.SyncJobRepository;
import com.govscheme.scheme.sync.SchemeFileImportService;
import com.govscheme.scheme.sync.SchemeSyncService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Admin operations. URL-level protection is configured in SecurityConfig
 * ({@code /admin/**} requires ROLE_ADMIN); method-level annotations provide
 * defense-in-depth so protection survives URL-matcher refactors.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SchemeSyncService syncService;
    private final SchemeFileImportService fileImportService;
    private final CriteriaBootstrapService bootstrapService;
    private final SyncJobRepository jobRepository;
    private final SyncErrorRepository errorRepository;

    public AdminController(SchemeSyncService syncService,
                           SchemeFileImportService fileImportService,
                           CriteriaBootstrapService bootstrapService,
                           SyncJobRepository jobRepository,
                           SyncErrorRepository errorRepository) {
        this.syncService = syncService;
        this.fileImportService = fileImportService;
        this.bootstrapService = bootstrapService;
        this.jobRepository = jobRepository;
        this.errorRepository = errorRepository;
    }

    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ping() {
        Map<String, Object> data = Map.of(
            "status", "OK",
            "timestamp", Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success(data, "Admin access confirmed"));
    }

    @PostMapping("/schemes/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyncJobResponse>> syncSchemes(
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(required = false) Integer limit) {
        SyncJob job = syncService.runSync(lang, from, limit);
        return ResponseEntity.ok(ApiResponse.success(SyncJobResponse.from(job), "Sync finished"));
    }

    @PostMapping("/schemes/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyncJobResponse>> importSchemes() {
        SyncJob job = fileImportService.importFile();
        return ResponseEntity.ok(ApiResponse.success(SyncJobResponse.from(job), "Import finished"));
    }

    @PostMapping("/schemes/criteria/bootstrap")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bootstrapCriteria() {
        CriteriaBootstrapService.BootstrapSummary summary = bootstrapService.bootstrap();
        Map<String, Object> data = Map.of(
            "scanned", summary.scanned(),
            "curated", summary.curated(),
            "alreadyCurated", summary.alreadyCurated(),
            "skipped", summary.skipped()
        );
        return ResponseEntity.ok(ApiResponse.success(data, "Criteria bootstrap finished"));
    }

    @GetMapping("/sync/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<SyncJobResponse>>> syncJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
            Sort.by(Sort.Direction.DESC, "startedAt"));
        Page<SyncJob> jobs = jobRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(jobs, SyncJobResponse::from)));
    }

    @GetMapping("/sync/errors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<SyncErrorResponse>>> syncErrors(
            @RequestParam String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
            Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<com.govscheme.scheme.entity.SyncError> errors = errorRepository.findByJobId(jobId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(errors, SyncErrorResponse::from)));
    }
}
