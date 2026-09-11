package com.idp.idpapi.workflow.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record WorkflowSubmitRequest(
        @NotEmpty(message = "approverIds khong duoc de trong.")
        List<@NotNull(message = "approverId khong hop le.") Integer> approverIds) {
}
