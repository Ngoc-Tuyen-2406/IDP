package com.idp.idpapi.contract.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ContractDetailResponse(
        Integer contractId,
        String contractNumber,
        String contractName,
        Integer documentTypeId,
        String documentTypeName,
        Integer partnerId,
        String partnerName,
        Integer uploadedById,
        String uploadedByName,
        String partnerRepresentativeName,
        String partnerRepresentativePosition,
        LocalDate signedDate,
        LocalDate effectiveDate,
        LocalDate expiredDate,
        BigDecimal totalValue,
        String currency,
        String status,
        Integer currentVersionId,
        Integer currentVersionNumber,
        boolean favorite,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ContractVersionResponse> versions,
        List<ContractFileResponse> files) {
}
