package com.idp.idpapi.role.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.idp.idpapi.permission.dto.response.PermissionResponse;
import com.idp.idpapi.role.dto.request.RoleUpsertRequest;
import com.idp.idpapi.role.dto.response.RoleDetailResponse;
import com.idp.idpapi.role.dto.response.RoleResponse;
import com.idp.idpapi.role.entity.Role;

@Component
public class RoleMapper {

    public void updateEntity(Role entity, RoleUpsertRequest request) {
        entity.setRoleName(request.roleName().trim().toUpperCase());
        entity.setDescription(request.description());
    }

    public RoleResponse toResponse(Role entity) {
        return new RoleResponse(
                entity.getRoleId(),
                entity.getRoleName(),
                entity.getDescription());
    }

    public RoleDetailResponse toDetailResponse(Role entity, List<PermissionResponse> permissions) {
        return new RoleDetailResponse(
                entity.getRoleId(),
                entity.getRoleName(),
                entity.getDescription(),
                permissions);
    }
}
