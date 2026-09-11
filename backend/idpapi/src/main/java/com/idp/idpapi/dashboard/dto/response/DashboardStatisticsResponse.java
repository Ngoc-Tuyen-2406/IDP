package com.idp.idpapi.dashboard.dto.response;

public record DashboardStatisticsResponse(
        long totalContracts,
        long draftContracts,
        long processingContracts,
        long pendingContracts,
        long approvedContracts,
        long rejectedContracts,
        long expiringThisMonth) {
}
