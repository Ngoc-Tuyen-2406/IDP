package com.idp.idpapi.processing.dto.request;

public record CreateProcessingJobRequest(
        Integer contractId,
        Integer versionId,
        String taskType) {
}
