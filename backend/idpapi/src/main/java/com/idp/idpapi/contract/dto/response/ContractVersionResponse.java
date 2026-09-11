package com.idp.idpapi.contract.dto.response;

import java.time.LocalDateTime;

public record ContractVersionResponse(
        Integer versionId,
        Integer versionNumber,
        String status,
        String changeNote,
        Boolean current,
        LocalDateTime createdAt) {
}
