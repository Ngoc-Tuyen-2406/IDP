package com.idp.idpapi.permission.mapper;

import org.springframework.stereotype.Component;

import com.idp.idpapi.permission.dto.request.PermissionUpsertRequest;
import com.idp.idpapi.permission.dto.response.PermissionResponse;
import com.idp.idpapi.permission.entity.Permission;

@Component
public class PermissionMapper {

    public void updateEntity(Permission entity, PermissionUpsertRequest request) {
        entity.setPermissionName(request.permissionName().trim());
        entity.setDescription(request.description());
    }

    public PermissionResponse toResponse(Permission entity) {
        return new PermissionResponse(
                entity.getPermissionId(),
                entity.getPermissionName(),
                entity.getDescription());
    }
}
