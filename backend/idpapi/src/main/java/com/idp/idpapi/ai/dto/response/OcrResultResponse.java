package com.idp.idpapi.ai.dto.response;

import java.time.LocalDateTime;

public record OcrResultResponse(
        Integer ocrId,
        Integer versionId,
        Integer pageNumber,
        String language,
        String engine,
        String ocrText,
        Double confidence,
        LocalDateTime createdAt) {
}
