package com.idp.idpapi.processing.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.idp.idpapi.ai.service.AIProcessingService;

@Component
public class ProcessingJobQueuedListener {

    private final AIProcessingService aiProcessingService;

    public ProcessingJobQueuedListener(AIProcessingService aiProcessingService) {
        this.aiProcessingService = aiProcessingService;
    }

    @Async("aiProcessingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobQueued(ProcessingJobQueuedEvent event) {
        aiProcessingService.processQueuedJob(event.jobId());
    }
}
