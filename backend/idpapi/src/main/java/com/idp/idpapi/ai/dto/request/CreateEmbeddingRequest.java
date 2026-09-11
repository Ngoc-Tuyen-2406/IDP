package com.idp.idpapi.ai.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateEmbeddingRequest(
        @NotNull(message = "contractId khong duoc de trong.")
        Integer contractId) {
}
