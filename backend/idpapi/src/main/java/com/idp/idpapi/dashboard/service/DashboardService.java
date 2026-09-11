package com.idp.idpapi.dashboard.service;

import java.util.List;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.dashboard.dto.response.DashboardStatisticsResponse;
import com.idp.idpapi.dashboard.dto.response.StatusMetricResponse;

public interface DashboardService {

    DashboardStatisticsResponse getStatistics(Integer currentUserId);

    List<StatusMetricResponse> getContractsByStatus();

    PageResponse<ContractSummaryResponse> getRecentContracts(Integer currentUserId, int limit);
}
