package com.idp.idpapi.role.service;

import java.util.List;

import com.idp.idpapi.permission.dto.response.PermissionResponse;
import com.idp.idpapi.role.dto.request.RolePermissionAssignRequest;
import com.idp.idpapi.role.dto.request.RoleUpsertRequest;
import com.idp.idpapi.role.dto.response.RoleDetailResponse;
import com.idp.idpapi.role.dto.response.RoleResponse;

public interface RoleService {

    List<RoleResponse> getAll();

    RoleDetailResponse getById(Integer roleId);

    RoleResponse create(RoleUpsertRequest request);

    RoleResponse update(Integer roleId, RoleUpsertRequest request);

    void delete(Integer roleId);

    List<PermissionResponse> getPermissions(Integer roleId);

    RoleDetailResponse assignPermissions(Integer roleId, RolePermissionAssignRequest request);

    void revokePermission(Integer roleId, Integer permissionId);
}
