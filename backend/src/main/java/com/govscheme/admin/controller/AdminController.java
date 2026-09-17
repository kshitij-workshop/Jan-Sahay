package com.govscheme.admin.controller;

import com.govscheme.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ping() {
        Map<String, Object> data = Map.of(
            "status", "OK",
            "timestamp", Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success(data, "Admin access confirmed"));
    }
}
