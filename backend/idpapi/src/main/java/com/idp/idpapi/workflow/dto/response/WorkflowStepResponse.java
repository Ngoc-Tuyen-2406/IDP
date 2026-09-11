package com.idp.idpapi.workflow.dto.response;

import java.time.LocalDateTime;

public record WorkflowStepResponse(
        Integer workflowId,
        Integer contractId,
        Integer stepNumber,
        Integer approverId,
        String approverName,
        String status,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime approvedAt) {
}
