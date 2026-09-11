package com.idp.idpapi.user.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserRoleAssignRequest(
        @NotEmpty(message = "roleIds khong duoc de trong.")
        Set<@NotNull(message = "roleId khong hop le.") Integer> roleIds) {
}
