package com.idp.idpapi.ai.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record MetadataUpdateRequest(
        @NotEmpty(message = "fields khong duoc de trong.")
        List<@Valid MetadataFieldUpdateRequest> fields) {
}
