package com.idp.idpapi.contract.dto.response;

import java.time.LocalDateTime;

public record ContractFileResponse(
        Integer fileId,
        String fileName,
        String fileType,
        Long fileSize,
        Integer pageCount,
        String fileHash,
        LocalDateTime uploadedAt) {
}
