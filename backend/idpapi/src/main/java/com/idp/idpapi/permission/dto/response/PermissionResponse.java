package com.idp.idpapi.permission.dto.response;

public record PermissionResponse(
        Integer permissionId,
        String permissionName,
        String description) {
}
