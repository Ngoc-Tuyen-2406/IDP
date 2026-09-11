package com.idp.idpapi.workflow.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.contract.dto.response.ContractDetailResponse;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.enums.ContractStatus;
import com.idp.idpapi.contract.repository.ContractRepository;
import com.idp.idpapi.contract.service.ContractService;
import com.idp.idpapi.notification.service.NotificationService;
import com.idp.idpapi.role.repository.RolePermissionRepository;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.repository.UserRepository;
import com.idp.idpapi.user.repository.UserRoleRepository;
import com.idp.idpapi.workflow.dto.request.WorkflowActionRequest;
import com.idp.idpapi.workflow.dto.request.WorkflowSubmitRequest;
import com.idp.idpapi.workflow.dto.response.WorkflowStepResponse;
import com.idp.idpapi.workflow.entity.ApprovalWorkflow;
import com.idp.idpapi.workflow.enums.ApprovalWorkflowStatus;
import com.idp.idpapi.workflow.mapper.ApprovalWorkflowMapper;
import com.idp.idpapi.workflow.repository.ApprovalWorkflowRepository;
import com.idp.idpapi.workflow.service.ApprovalWorkflowService;

@Service
@Transactional
public class ApprovalWorkflowServiceImpl implements ApprovalWorkflowService {

    private final ApprovalWorkflowRepository approvalWorkflowRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ApprovalWorkflowMapper approvalWorkflowMapper;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public ApprovalWorkflowServiceImpl(
            ApprovalWorkflowRepository approvalWorkflowRepository,
            ContractRepository contractRepository,
            ContractService contractService,
            UserRepository userRepository,
            NotificationService notificationService,
            ApprovalWorkflowMapper approvalWorkflowMapper,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository) {
        this.approvalWorkflowRepository = approvalWorkflowRepository;
        this.contractRepository = contractRepository;
        this.contractService = contractService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.approvalWorkflowMapper = approvalWorkflowMapper;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowStepResponse> getWorkflow(Integer contractId) {
        getContract(contractId);
        return approvalWorkflowRepository.findByContractContractIdOrderByStepNumberAscCreatedAtAsc(contractId)
                .stream()
                .map(approvalWorkflowMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContractSummaryResponse> getPendingContracts(Integer approverId, Pageable pageable) {
        var page = approvalWorkflowRepository.findByApproverUserIdAndStatusOrderByCreatedAtDesc(
                approverId,
                ApprovalWorkflowStatus.PENDING,
                pageable);
        var summaries = page.getContent().stream()
                .map(workflow -> contractService.getById(workflow.getContract().getContractId(), approverId))
                .map(this::toSummary)
                .toList();
        return PageResponse.from(new PageImpl<>(summaries, pageable, page.getTotalElements()));
    }

    @Override
    public List<WorkflowStepResponse> submit(Integer contractId, WorkflowSubmitRequest request, Integer currentUserId) {
        Contract contract = getContract(contractId);
        Map<Integer, User> approvers = userRepository.findAllById(request.approverIds())
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
        if (approvers.size() != request.approverIds().size()) {
            throw new ResourceNotFoundException("Co nguoi duyet khong ton tai.");
        }
        for (Integer approverId : request.approverIds()) {
            List<Integer> roleIds = userRoleRepository.findRoleIdsByUserId(approverId);
            if (roleIds.isEmpty()
                    || !rolePermissionRepository.findPermissionNamesByRoleIds(roleIds).contains("CONTRACT_APPROVE")) {
                throw new BadRequestException("Nguoi duyet duoc chon khong co quyen CONTRACT_APPROVE.");
            }
        }

        approvalWorkflowRepository.deleteByContractContractId(contractId);
        for (int index = 0; index < request.approverIds().size(); index++) {
            Integer approverId = request.approverIds().get(index);
            ApprovalWorkflow workflow = new ApprovalWorkflow();
            workflow.setContract(contract);
            workflow.setStepNumber(index + 1);
            workflow.setApprover(approvers.get(approverId));
            workflow.setStatus(ApprovalWorkflowStatus.PENDING);
            approvalWorkflowRepository.save(workflow);
        }

        contract.setStatus(ContractStatus.PENDING);
        contractRepository.save(contract);
        Integer firstApproverId = request.approverIds().get(0);
        notificationService.createNotification(
                firstApproverId,
                "Hop dong can phe duyet",
                "Hop dong " + contract.getContractNumber() + " da duoc gui den ban de phe duyet.",
                "APPROVAL_PENDING",
                "/contracts/" + contractId);
        return getWorkflow(contractId);
    }

    @Override
    public List<WorkflowStepResponse> approve(Integer contractId, WorkflowActionRequest request, Integer approverId) {
        Contract contract = getContract(contractId);
        ApprovalWorkflow workflow = getCurrentStepForApprover(contractId, approverId);
        workflow.setStatus(ApprovalWorkflowStatus.APPROVED);
        workflow.setComment(request.comment());
        workflow.setApprovedAt(LocalDateTime.now());
        approvalWorkflowRepository.save(workflow);

        List<ApprovalWorkflow> steps = approvalWorkflowRepository.findByContractContractIdOrderByStepNumberAscCreatedAtAsc(contractId);
        ApprovalWorkflow nextPending = steps.stream()
                .filter(step -> step.getStatus() == ApprovalWorkflowStatus.PENDING)
                .findFirst()
                .orElse(null);
        if (nextPending == null) {
            contract.setStatus(ContractStatus.APPROVED);
            contractRepository.save(contract);
            notificationService.createNotification(
                    contract.getUploadedBy().getUserId(),
                    "Hop dong da duoc phe duyet",
                    "Hop dong " + contract.getContractNumber() + " da duoc phe duyet hoan tat.",
                    "APPROVAL_APPROVED",
                    "/contracts/" + contractId);
        } else {
            notificationService.createNotification(
                    nextPending.getApprover().getUserId(),
                    "Hop dong can phe duyet",
                    "Hop dong " + contract.getContractNumber() + " dang cho ban duyet o buoc tiep theo.",
                    "APPROVAL_PENDING",
                    "/contracts/" + contractId);
        }

        return getWorkflow(contractId);
    }

    @Override
    public List<WorkflowStepResponse> reject(Integer contractId, WorkflowActionRequest request, Integer approverId) {
        Contract contract = getContract(contractId);
        ApprovalWorkflow workflow = getCurrentStepForApprover(contractId, approverId);
        workflow.setStatus(ApprovalWorkflowStatus.REJECTED);
        workflow.setComment(request.comment());
        workflow.setApprovedAt(LocalDateTime.now());
        approvalWorkflowRepository.save(workflow);

        approvalWorkflowRepository.findByContractContractIdOrderByStepNumberAscCreatedAtAsc(contractId)
                .stream()
                .filter(step -> step.getStatus() == ApprovalWorkflowStatus.PENDING)
                .forEach(step -> {
                    step.setStatus(ApprovalWorkflowStatus.SKIPPED);
                    step.setComment("Bo qua do hop dong da bi tu choi.");
                    step.setApprovedAt(LocalDateTime.now());
                    approvalWorkflowRepository.save(step);
                });

        contract.setStatus(ContractStatus.REJECTED);
        contractRepository.save(contract);
        notificationService.createNotification(
                contract.getUploadedBy().getUserId(),
                "Hop dong bi tu choi",
                "Hop dong " + contract.getContractNumber() + " da bi tu choi trong quy trinh phe duyet.",
                "APPROVAL_REJECTED",
                "/contracts/" + contractId);
        return getWorkflow(contractId);
    }

    private Contract getContract(Integer contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay hop dong."));
    }

    private ApprovalWorkflow getCurrentStepForApprover(Integer contractId, Integer approverId) {
        ApprovalWorkflow currentStep = approvalWorkflowRepository
                .findByContractContractIdOrderByStepNumberAscCreatedAtAsc(contractId)
                .stream()
                .filter(step -> step.getStatus() == ApprovalWorkflowStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay buoc duyet dang cho."));
        if (!currentStep.getApprover().getUserId().equals(approverId)) {
            throw new AccessDeniedException("Ban khong phai nguoi duyet cua buoc hien tai.");
        }
        return currentStep;
    }

    private ContractSummaryResponse toSummary(ContractDetailResponse detail) {
        String latestFileName = detail.files().isEmpty() ? null : detail.files().get(0).fileName();
        return new ContractSummaryResponse(
                detail.contractId(),
                detail.contractNumber(),
                detail.contractName(),
                detail.documentTypeId(),
                detail.documentTypeName(),
                detail.partnerId(),
                detail.partnerName(),
                detail.status(),
                detail.effectiveDate(),
                detail.expiredDate(),
                detail.totalValue(),
                detail.currency(),
                detail.currentVersionId(),
                detail.currentVersionNumber(),
                latestFileName,
                detail.favorite(),
                detail.createdAt(),
                detail.updatedAt());
    }
}
