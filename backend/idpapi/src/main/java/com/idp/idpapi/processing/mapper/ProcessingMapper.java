package com.idp.idpapi.processing.mapper;

import org.springframework.stereotype.Component;

import com.idp.idpapi.processing.dto.response.ProcessingJobResponse;
import com.idp.idpapi.processing.entity.ProcessingQueue;

@Component
public class ProcessingMapper {

    public ProcessingJobResponse toResponse(ProcessingQueue entity) {
        return new ProcessingJobResponse(
                entity.getQueueId(),
                entity.getVersion().getVersionId(),
                entity.getVersion().getContract().getContractId(),
                entity.getTaskType(),
                entity.getQueueName(),
                entity.getWorkerName(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getRetryCount(),
                entity.getProgress(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getStartedAt(),
                entity.getFinishedAt());
    }
}
