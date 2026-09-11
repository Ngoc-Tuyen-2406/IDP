package com.idp.idpapi.ai.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

public record ContractClauseResponse(
        Integer clauseId,
        Integer versionId,
        String clauseType,
        String title,
        String clauseText,
        JsonNode matchedKeywords,
        Integer pageNumber,
        Double confidence,
        String modelName,
        LocalDateTime createdAt) {
}
