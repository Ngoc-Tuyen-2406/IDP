package com.idp.idpapi.contract.enums;

import java.util.Arrays;

public enum ContractVersionStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String value;

    ContractVersionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ContractVersionStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported contract version status: " + value));
    }
}
