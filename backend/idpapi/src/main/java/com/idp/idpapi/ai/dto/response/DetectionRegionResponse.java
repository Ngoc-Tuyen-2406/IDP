package com.idp.idpapi.ai.dto.response;

public record DetectionRegionResponse(
        Integer regionId,
        Integer versionId,
        Integer pageNumber,
        String label,
        Double xMin,
        Double yMin,
        Double xMax,
        Double yMax,
        Double confidence) {
}
