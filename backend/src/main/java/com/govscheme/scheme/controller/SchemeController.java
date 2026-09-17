package com.govscheme.scheme.controller;

import com.govscheme.common.dto.ApiResponse;
import com.govscheme.common.dto.PageResponse;
import com.govscheme.scheme.dto.SchemeDetailResponse;
import com.govscheme.scheme.dto.SchemeSummaryResponse;
import com.govscheme.scheme.service.SchemeQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public scheme catalog browsing. Demographic eligibility filters live in the
 * eligibility engine (Phase 6); browsing filters are textual and categorical.
 */
@RestController
@RequestMapping("/api/schemes")
public class SchemeController {

    private final SchemeQueryService queryService;

    public SchemeController(SchemeQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SchemeSummaryResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<SchemeSummaryResponse> result =
            queryService.search(q, category, state, level, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchemeDetailResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getById(id)));
    }
}
