package com.idp.idpapi.role.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RolePermissionAssignRequest(
        @NotEmpty(message = "permissionIds khong duoc de trong.")
        Set<@NotNull(message = "permissionId khong hop le.") Integer> permissionIds) {
}
