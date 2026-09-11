package com.idp.idpapi.processing.dto.response;

import java.time.LocalDateTime;

public record ProcessingJobResponse(
        Integer queueId,
        Integer versionId,
        Integer contractId,
        String taskType,
        String queueName,
        String workerName,
        String status,
        Integer priority,
        Integer retryCount,
        Short progress,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt) {
}
