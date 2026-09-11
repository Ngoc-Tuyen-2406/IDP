package com.idp.idpapi.workflow.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.repository.ContractRepository;
import com.idp.idpapi.contract.service.ContractService;
import com.idp.idpapi.notification.service.NotificationService;
import com.idp.idpapi.role.repository.RolePermissionRepository;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.repository.UserRepository;
import com.idp.idpapi.user.repository.UserRoleRepository;
import com.idp.idpapi.workflow.dto.request.WorkflowActionRequest;
import com.idp.idpapi.workflow.dto.request.WorkflowSubmitRequest;
import com.idp.idpapi.workflow.entity.ApprovalWorkflow;
import com.idp.idpapi.workflow.enums.ApprovalWorkflowStatus;
import com.idp.idpapi.workflow.mapper.ApprovalWorkflowMapper;
import com.idp.idpapi.workflow.repository.ApprovalWorkflowRepository;

@ExtendWith(MockitoExtension.class)
class ApprovalWorkflowServiceImplTest {

    @Mock
    private ApprovalWorkflowRepository approvalWorkflowRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractService contractService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApprovalWorkflowMapper approvalWorkflowMapper;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    private ApprovalWorkflowServiceImpl workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new ApprovalWorkflowServiceImpl(
                approvalWorkflowRepository,
                contractRepository,
                contractService,
                userRepository,
                notificationService,
                approvalWorkflowMapper,
                userRoleRepository,
                rolePermissionRepository);
    }

    @Test
    void submitShouldRejectApproverWithoutContractApprovePermission() {
        Contract contract = contract(1);
        User employee = user(2);

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(userRepository.findAllById(List.of(2))).thenReturn(List.of(employee));
        when(userRoleRepository.findRoleIdsByUserId(2)).thenReturn(List.of(3));
        when(rolePermissionRepository.findPermissionNamesByRoleIds(List.of(3)))
                .thenReturn(Set.of("CONTRACT_VIEW", "CONTRACT_UPDATE"));

        assertThrows(
                BadRequestException.class,
                () -> workflowService.submit(1, new WorkflowSubmitRequest(List.of(2)), 99));

        verify(approvalWorkflowRepository, never()).deleteByContractContractId(1);
        verify(approvalWorkflowRepository, never()).save(org.mockito.ArgumentMatchers.any(ApprovalWorkflow.class));
    }

    @Test
    void approveShouldRejectUserWhoIsNotAssignedToCurrentStep() {
        Contract contract = contract(1);
        User assignedManager = user(10);
        ApprovalWorkflow currentStep = new ApprovalWorkflow();
        currentStep.setContract(contract);
        currentStep.setStepNumber(1);
        currentStep.setApprover(assignedManager);
        currentStep.setStatus(ApprovalWorkflowStatus.PENDING);

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(approvalWorkflowRepository.findByContractContractIdOrderByStepNumberAscCreatedAtAsc(1))
                .thenReturn(List.of(currentStep));

        assertThrows(
                AccessDeniedException.class,
                () -> workflowService.approve(1, new WorkflowActionRequest("approved"), 11));

        verify(approvalWorkflowRepository, never()).save(currentStep);
    }

    private Contract contract(Integer id) {
        Contract contract = new Contract();
        contract.setContractId(id);
        contract.setContractNumber("IDP-TEST-" + id);
        return contract;
    }

    private User user(Integer id) {
        User user = new User();
        user.setUserId(id);
        return user;
    }
}
