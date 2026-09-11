package com.idp.idpapi.ai.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

public record MetadataFieldResponse(
        Integer metadataId,
        Integer versionId,
        String fieldName,
        String fieldType,
        JsonNode originalValue,
        JsonNode currentValue,
        Double confidence,
        Boolean verified,
        Integer verifiedBy,
        LocalDateTime verifiedAt) {
}
