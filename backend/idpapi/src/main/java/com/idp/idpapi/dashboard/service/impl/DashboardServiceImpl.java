package com.idp.idpapi.dashboard.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.contract.enums.ContractStatus;
import com.idp.idpapi.contract.service.ContractService;
import com.idp.idpapi.dashboard.dto.response.DashboardStatisticsResponse;
import com.idp.idpapi.dashboard.dto.response.StatusMetricResponse;
import com.idp.idpapi.dashboard.service.DashboardService;
import com.idp.idpapi.contract.repository.ContractRepository;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ContractRepository contractRepository;
    private final ContractService contractService;
    private final Clock clock;

    public DashboardServiceImpl(
            ContractRepository contractRepository,
            ContractService contractService,
            Clock clock) {
        this.contractRepository = contractRepository;
        this.contractService = contractService;
        this.clock = clock;
    }

    @Override
    public DashboardStatisticsResponse getStatistics(Integer currentUserId) {
        LocalDate today = LocalDate.now(clock);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        return new DashboardStatisticsResponse(
                contractRepository.count(),
                contractRepository.countByStatus(ContractStatus.DRAFT),
                contractRepository.countByStatus(ContractStatus.PROCESSING),
                contractRepository.countByStatus(ContractStatus.PENDING),
                contractRepository.countByStatus(ContractStatus.APPROVED),
                contractRepository.countByStatus(ContractStatus.REJECTED),
                contractRepository.countByExpiredDateBetween(today, endOfMonth));
    }

    @Override
    public List<StatusMetricResponse> getContractsByStatus() {
        return contractRepository.countByStatusGrouped()
                .stream()
                .map(row -> new StatusMetricResponse(row[0] != null ? row[0].toString() : "Unknown", (Long) row[1]))
                .toList();
    }

    @Override
    public PageResponse<ContractSummaryResponse> getRecentContracts(Integer currentUserId, int limit) {
        return PageResponse.from(contractRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .map(contract -> contractService.getById(contract.getContractId(), currentUserId))
                .map(this::toSummary));
    }

    private ContractSummaryResponse toSummary(com.idp.idpapi.contract.dto.response.ContractDetailResponse detail) {
        String latestFileName = detail.files().isEmpty() ? null : detail.files().get(0).fileName();
        return new ContractSummaryResponse(
                detail.contractId(),
                detail.contractNumber(),
                detail.contractName(),
                detail.documentTypeId(),
                detail.documentTypeName(),
                detail.partnerId(),
                detail.partnerName(),
                detail.status(),
                detail.effectiveDate(),
                detail.expiredDate(),
                detail.totalValue(),
                detail.currency(),
                detail.currentVersionId(),
                detail.currentVersionNumber(),
                latestFileName,
                detail.favorite(),
                detail.createdAt(),
                detail.updatedAt());
    }
}
