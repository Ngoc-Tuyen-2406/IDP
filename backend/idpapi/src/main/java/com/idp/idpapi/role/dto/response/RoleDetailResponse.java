package com.idp.idpapi.role.dto.response;

import java.util.List;

import com.idp.idpapi.permission.dto.response.PermissionResponse;

public record RoleDetailResponse(
        Integer roleId,
        String roleName,
        String description,
        List<PermissionResponse> permissions) {
}
