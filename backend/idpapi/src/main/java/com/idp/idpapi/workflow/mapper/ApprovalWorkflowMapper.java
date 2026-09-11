package com.idp.idpapi.workflow.mapper;

import org.springframework.stereotype.Component;

import com.idp.idpapi.workflow.dto.response.WorkflowStepResponse;
import com.idp.idpapi.workflow.entity.ApprovalWorkflow;

@Component
public class ApprovalWorkflowMapper {

    public WorkflowStepResponse toResponse(ApprovalWorkflow workflow) {
        return new WorkflowStepResponse(
                workflow.getWorkflowId(),
                workflow.getContract().getContractId(),
                workflow.getStepNumber(),
                workflow.getApprover().getUserId(),
                workflow.getApprover().getFullName(),
                workflow.getStatus() != null ? workflow.getStatus().getValue() : null,
                workflow.getComment(),
                workflow.getCreatedAt(),
                workflow.getApprovedAt());
    }
}
