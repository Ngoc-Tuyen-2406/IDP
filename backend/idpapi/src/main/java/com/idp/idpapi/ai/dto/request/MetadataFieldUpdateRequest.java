package com.idp.idpapi.ai.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MetadataFieldUpdateRequest(
        Integer metadataId,

        @NotBlank(message = "fieldName khong duoc de trong.")
        @Size(max = 100, message = "fieldName khong duoc vuot qua 100 ky tu.")
        String fieldName,

        @Size(max = 50, message = "fieldType khong duoc vuot qua 50 ky tu.")
        String fieldType,

        @NotNull(message = "currentValue khong duoc de trong.")
        JsonNode currentValue,

        Boolean verified) {
}
