package com.idp.idpapi.workflow.dto.request;

import jakarta.validation.constraints.Size;

public record WorkflowActionRequest(
        @Size(max = 2000, message = "comment khong duoc vuot qua 2000 ky tu.")
        String comment) {
}
