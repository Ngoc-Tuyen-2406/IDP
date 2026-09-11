package com.idp.idpapi.contract.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContractSummaryResponse(
        Integer contractId,
        String contractNumber,
        String contractName,
        Integer documentTypeId,
        String documentTypeName,
        Integer partnerId,
        String partnerName,
        String status,
        LocalDate effectiveDate,
        LocalDate expiredDate,
        BigDecimal totalValue,
        String currency,
        Integer currentVersionId,
        Integer currentVersionNumber,
        String latestFileName,
        boolean favorite,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
