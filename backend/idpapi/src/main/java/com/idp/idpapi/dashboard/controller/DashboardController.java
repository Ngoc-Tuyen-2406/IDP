package com.idp.idpapi.dashboard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.dashboard.dto.response.DashboardStatisticsResponse;
import com.idp.idpapi.dashboard.dto.response.StatusMetricResponse;
import com.idp.idpapi.dashboard.service.DashboardService;
import com.idp.idpapi.security.SecurityUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> statistics(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thống kê dashboard thành công.",
                dashboardService.getStatistics(resolveUserId(currentUser))));
    }

    @GetMapping("/contracts-by-status")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<List<StatusMetricResponse>>> contractsByStatus() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thống kê theo trạng thái thành công.",
                dashboardService.getContractsByStatus()));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<ContractSummaryResponse>>> recent(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy hợp đồng gần đây thành công.",
                dashboardService.getRecentContracts(resolveUserId(currentUser), limit)));
    }

    private Integer resolveUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phiên đăng nhập không hợp lệ.");
        }
        return currentUser.getUserId();
    }
}
