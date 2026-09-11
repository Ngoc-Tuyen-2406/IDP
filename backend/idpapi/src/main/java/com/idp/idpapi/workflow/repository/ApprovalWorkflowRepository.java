package com.idp.idpapi.workflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.workflow.entity.ApprovalWorkflow;
import com.idp.idpapi.workflow.enums.ApprovalWorkflowStatus;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Integer> {

    List<ApprovalWorkflow> findByContractContractIdOrderByStepNumberAscCreatedAtAsc(Integer contractId);

    Optional<ApprovalWorkflow> findFirstByContractContractIdAndApproverUserIdAndStatusOrderByStepNumberAscCreatedAtAsc(
            Integer contractId,
            Integer approverId,
            ApprovalWorkflowStatus status);

    Page<ApprovalWorkflow> findByApproverUserIdAndStatusOrderByCreatedAtDesc(
            Integer approverId,
            ApprovalWorkflowStatus status,
            Pageable pageable);

    boolean existsByContractContractIdAndStatus(Integer contractId, ApprovalWorkflowStatus status);

    void deleteByContractContractId(Integer contractId);
}
