package com.idp.idpapi.user.dto.request;

import com.idp.idpapi.user.entity.UserStatus;

import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull(message = "status khong duoc de trong.")
        UserStatus status) {
}
