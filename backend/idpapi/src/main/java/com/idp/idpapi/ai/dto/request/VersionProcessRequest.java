package com.idp.idpapi.ai.dto.request;

import jakarta.validation.constraints.NotNull;

public record VersionProcessRequest(
        @NotNull(message = "versionId khong duoc de trong.")
        Integer versionId) {
}
