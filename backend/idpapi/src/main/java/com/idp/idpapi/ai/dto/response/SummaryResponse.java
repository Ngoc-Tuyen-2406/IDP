package com.idp.idpapi.ai.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

public record SummaryResponse(
        Integer summaryId,
        Integer versionId,
        String summary,
        JsonNode summaryJson,
        String modelName,
        LocalDateTime createdAt) {
}
