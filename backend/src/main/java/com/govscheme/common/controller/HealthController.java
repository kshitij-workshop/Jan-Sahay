package com.govscheme.common.controller;

import com.govscheme.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = Map.of(
            "status", "UP",
            "timestamp", Instant.now().toString(),
            "service", "government-scheme-assistant",
            "version", "1.0.0-SNAPSHOT"
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readiness() {
        Map<String, Object> data = Map.of(
            "status", "READY",
            "timestamp", Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/live")
    public ResponseEntity<ApiResponse<Map<String, Object>>> liveness() {
        Map<String, Object> data = Map.of(
            "status", "ALIVE",
            "timestamp", Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}