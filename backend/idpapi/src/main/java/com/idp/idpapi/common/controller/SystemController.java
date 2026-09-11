package com.idp.idpapi.common.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.common.api.ApiResponse;

@RestController
@RequestMapping("/api/v1")
public class SystemController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return ResponseEntity.ok(ApiResponse.success(
                "Health check thanh cong.",
                Map.of("status", "UP", "service", "idp-backend")));
    }

    @GetMapping("/version")
    public ResponseEntity<ApiResponse<Map<String, Object>>> version() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay version thanh cong.",
                Map.of("version", "v1", "buildDate", LocalDate.of(2026, 7, 27).toString())));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay trang thai he thong thanh cong.",
                Map.of("backend", "READY", "apiPrefix", "/api/v1")));
    }
}
