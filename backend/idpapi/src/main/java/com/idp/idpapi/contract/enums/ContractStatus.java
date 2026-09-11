package com.idp.idpapi.contract.enums;

import java.util.Arrays;

public enum ContractStatus {
    DRAFT("Draft"),
    PROCESSING("Processing"),
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    TERMINATED("Terminated");

    private final String value;

    ContractStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ContractStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported contract status: " + value));
    }
}
