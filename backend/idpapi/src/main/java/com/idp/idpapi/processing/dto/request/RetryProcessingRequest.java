package com.idp.idpapi.processing.dto.request;

import jakarta.validation.constraints.NotNull;

public record RetryProcessingRequest(
        @NotNull(message = "jobId khong duoc de trong.")
        Integer jobId) {
}
