package com.idp.idpapi.processing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.ai.service.AIProcessingService;
import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.processing.dto.request.CreateProcessingJobRequest;
import com.idp.idpapi.processing.dto.request.RetryProcessingRequest;
import com.idp.idpapi.processing.dto.response.ProcessingJobResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Processing Jobs")
public class ProcessingController {

    private final AIProcessingService aiProcessingService;

    public ProcessingController(AIProcessingService aiProcessingService) {
        this.aiProcessingService = aiProcessingService;
    }

    @PostMapping({"/ai/jobs", "/processing/jobs"})
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> createJob(
            @RequestBody CreateProcessingJobRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao job xu ly thanh cong.",
                aiProcessingService.createJob(request)));
    }

    @GetMapping({"/ai/jobs", "/processing/jobs"})
    @PreAuthorize("hasAuthority('PROCESSING_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<ProcessingJobResponse>>> getJobs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach job xu ly thanh cong.",
                aiProcessingService.getJobs(page, size)));
    }

    @GetMapping({"/ai/jobs/{jobId}", "/processing/jobs/{jobId}"})
    @PreAuthorize("hasAuthority('PROCESSING_VIEW')")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> getJob(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay chi tiet job xu ly thanh cong.",
                aiProcessingService.getJob(jobId)));
    }

    @DeleteMapping("/ai/jobs/{jobId}")
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Integer jobId) {
        aiProcessingService.deleteJob(jobId);
        return ResponseEntity.ok(ApiResponse.success("Xoa job xu ly thanh cong."));
    }

    @PostMapping("/processing/retry")
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> retry(
            @Valid @RequestBody RetryProcessingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Retry job xu ly thanh cong.",
                aiProcessingService.retry(request.jobId())));
    }
}
