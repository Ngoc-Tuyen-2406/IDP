package com.idp.idpapi.ai.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

public record RiskAnalysisResponse(
        Integer riskId,
        Integer versionId,
        String riskLevel,
        Double riskScore,
        String riskSummary,
        String recommendation,
        JsonNode riskDetails,
        String modelName,
        LocalDateTime createdAt) {
}
