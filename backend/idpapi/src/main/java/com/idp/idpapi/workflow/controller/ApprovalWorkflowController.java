package com.idp.idpapi.workflow.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.security.SecurityUserDetails;
import com.idp.idpapi.workflow.dto.request.WorkflowActionRequest;
import com.idp.idpapi.workflow.dto.request.WorkflowSubmitRequest;
import com.idp.idpapi.workflow.dto.response.WorkflowStepResponse;
import com.idp.idpapi.workflow.service.ApprovalWorkflowService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/contracts")
@Tag(name = "Approval Workflow")
public class ApprovalWorkflowController {

    private final ApprovalWorkflowService approvalWorkflowService;

    public ApprovalWorkflowController(ApprovalWorkflowService approvalWorkflowService) {
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @GetMapping("/{contractId}/workflow")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> getWorkflow(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay workflow phe duyet thanh cong.",
                approvalWorkflowService.getWorkflow(contractId)));
    }

    @PostMapping("/{contractId}/submit")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> submit(
            @PathVariable Integer contractId,
            @Valid @RequestBody WorkflowSubmitRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Gui hop dong vao quy trinh phe duyet thanh cong.",
                approvalWorkflowService.submit(contractId, request, extractUserId(currentUser))));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ResponseEntity<ApiResponse<PageResponse<ContractSummaryResponse>>> getPendingContracts(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach hop dong cho phe duyet thanh cong.",
                approvalWorkflowService.getPendingContracts(extractUserId(currentUser), pageable)));
    }

    @PostMapping("/{contractId}/approve")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> approve(
            @PathVariable Integer contractId,
            @Valid @RequestBody WorkflowActionRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Phe duyet hop dong thanh cong.",
                approvalWorkflowService.approve(contractId, request, extractUserId(currentUser))));
    }

    @PostMapping("/{contractId}/reject")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> reject(
            @PathVariable Integer contractId,
            @Valid @RequestBody WorkflowActionRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tu choi hop dong thanh cong.",
                approvalWorkflowService.reject(contractId, request, extractUserId(currentUser))));
    }

    @GetMapping("/{contractId}/approval-history")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> getApprovalHistory(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay lich su phe duyet thanh cong.",
                approvalWorkflowService.getWorkflow(contractId)));
    }

    private Integer extractUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phien dang nhap khong hop le.");
        }
        return currentUser.getUserId();
    }
}
