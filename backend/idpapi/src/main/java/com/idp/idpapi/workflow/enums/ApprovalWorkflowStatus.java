package com.idp.idpapi.workflow.enums;

import java.util.Arrays;

public enum ApprovalWorkflowStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    SKIPPED("Skipped");

    private final String value;

    ApprovalWorkflowStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ApprovalWorkflowStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported approval workflow status: " + value));
    }
}
