package com.idp.idpapi.ai.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.ai.dto.request.MetadataUpdateRequest;
import com.idp.idpapi.ai.dto.request.VersionProcessRequest;
import com.idp.idpapi.ai.dto.response.DetectionRegionResponse;
import com.idp.idpapi.ai.dto.response.ContractClauseResponse;
import com.idp.idpapi.ai.dto.response.EmbeddingInfoResponse;
import com.idp.idpapi.ai.dto.response.MetadataFieldResponse;
import com.idp.idpapi.ai.dto.response.OcrResultResponse;
import com.idp.idpapi.ai.dto.response.RiskAnalysisResponse;
import com.idp.idpapi.ai.dto.response.SummaryResponse;
import com.idp.idpapi.ai.service.AIProcessingService;
import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.security.SecurityUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "AI Processing")
public class AIController {

    private final AIProcessingService aiProcessingService;

    public AIController(AIProcessingService aiProcessingService) {
        this.aiProcessingService = aiProcessingService;
    }

    @PostMapping({"/ocr/run", "/ai/ocr"})
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<List<OcrResultResponse>>> runOcr(
            @Valid @RequestBody VersionProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Chay OCR thanh cong.",
                aiProcessingService.runOcr(request)));
    }

    @GetMapping("/ocr/{versionId}")
    @PreAuthorize("hasAuthority('AI_REVIEW')")
    public ResponseEntity<ApiResponse<List<OcrResultResponse>>> getOcr(@PathVariable Integer versionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay ket qua OCR thanh cong.",
                aiProcessingService.getOcr(versionId)));
    }

    @PostMapping({"/detection/run", "/ai/detect-layout"})
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<List<DetectionRegionResponse>>> runDetection(
            @Valid @RequestBody VersionProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Chay detect layout thanh cong.",
                aiProcessingService.runDetection(request)));
    }

    @GetMapping("/detection/{versionId}")
    @PreAuthorize("hasAuthority('AI_REVIEW')")
    public ResponseEntity<ApiResponse<List<DetectionRegionResponse>>> getDetection(@PathVariable Integer versionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay ket qua detect thanh cong.",
                aiProcessingService.getDetection(versionId)));
    }

    @PostMapping("/contracts/{contractId}/clauses/extract")
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<List<ContractClauseResponse>>> extractClauses(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Trich xuat dieu khoan thanh cong.",
                aiProcessingService.extractClauses(contractId)));
    }

    @GetMapping("/contracts/{contractId}/clauses")
    @PreAuthorize("hasAuthority('AI_REVIEW')")
    public ResponseEntity<ApiResponse<List<ContractClauseResponse>>> getClauses(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay dieu khoan thanh cong.",
                aiProcessingService.getClauses(contractId)));
    }

    @GetMapping("/contracts/{contractId}/metadata")
    @PreAuthorize("hasAuthority('AI_REVIEW')")
    public ResponseEntity<ApiResponse<List<MetadataFieldResponse>>> getMetadata(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay metadata thanh cong.",
                aiProcessingService.getMetadataByContract(contractId)));
    }

    @PutMapping("/contracts/{contractId}/metadata")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    public ResponseEntity<ApiResponse<List<MetadataFieldResponse>>> updateMetadata(
            @PathVariable Integer contractId,
            @Valid @RequestBody MetadataUpdateRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat metadata thanh cong.",
                aiProcessingService.updateMetadata(contractId, request, extractUserId(currentUser))));
    }

    @DeleteMapping("/contracts/{contractId}/metadata")
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<Void>> deleteMetadata(@PathVariable Integer contractId) {
        aiProcessingService.deleteMetadata(contractId);
        return ResponseEntity.ok(ApiResponse.success("Xoa metadata thanh cong."));
    }

    @PostMapping("/contracts/{contractId}/summary")
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<SummaryResponse>> generateSummary(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao tom tat hop dong thanh cong.",
                aiProcessingService.generateSummary(contractId)));
    }

    @GetMapping("/contracts/{contractId}/summary")
    @PreAuthorize("hasAuthority('AI_REVIEW')")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummary(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay tom tat hop dong thanh cong.",
                aiProcessingService.getSummary(contractId)));
    }

    @PostMapping({"/contracts/{contractId}/risk-analysis", "/ai/analyze"})
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<RiskAnalysisResponse>> analyzeRisk(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Phan tich rui ro hop dong thanh cong.",
                aiProcessingService.analyzeRisk(contractId)));
    }

    @GetMapping("/contracts/{contractId}/risk-report")
    @PreAuthorize("hasAuthority('AI_REVIEW')")
    public ResponseEntity<ApiResponse<RiskAnalysisResponse>> getRisk(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay bao cao rui ro thanh cong.",
                aiProcessingService.getRisk(contractId)));
    }

    @PostMapping("/embeddings/create")
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<EmbeddingInfoResponse>> createEmbedding(
            @Valid @RequestBody com.idp.idpapi.ai.dto.request.CreateEmbeddingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao embedding thanh cong.",
                aiProcessingService.createEmbeddings(request.contractId())));
    }

    @GetMapping("/embeddings/{contractId}")
    @PreAuthorize("hasAuthority('AI_REVIEW')")
    public ResponseEntity<ApiResponse<EmbeddingInfoResponse>> getEmbedding(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay embedding thanh cong.",
                aiProcessingService.getEmbeddings(contractId)));
    }

    @DeleteMapping("/embeddings/{contractId}")
    @PreAuthorize("hasAuthority('AI_PROCESS')")
    public ResponseEntity<ApiResponse<Void>> deleteEmbedding(@PathVariable Integer contractId) {
        aiProcessingService.deleteEmbeddings(contractId);
        return ResponseEntity.ok(ApiResponse.success("Xoa embedding thanh cong."));
    }

    private Integer extractUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phien dang nhap khong hop le.");
        }
        return currentUser.getUserId();
    }
}
