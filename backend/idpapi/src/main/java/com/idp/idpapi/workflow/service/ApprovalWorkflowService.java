package com.idp.idpapi.workflow.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.workflow.dto.request.WorkflowActionRequest;
import com.idp.idpapi.workflow.dto.request.WorkflowSubmitRequest;
import com.idp.idpapi.workflow.dto.response.WorkflowStepResponse;

public interface ApprovalWorkflowService {

    List<WorkflowStepResponse> getWorkflow(Integer contractId);

    PageResponse<ContractSummaryResponse> getPendingContracts(Integer approverId, Pageable pageable);

    List<WorkflowStepResponse> submit(Integer contractId, WorkflowSubmitRequest request, Integer currentUserId);

    List<WorkflowStepResponse> approve(Integer contractId, WorkflowActionRequest request, Integer approverId);

    List<WorkflowStepResponse> reject(Integer contractId, WorkflowActionRequest request, Integer approverId);
}
