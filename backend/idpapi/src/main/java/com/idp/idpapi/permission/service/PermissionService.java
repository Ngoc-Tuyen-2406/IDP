package com.idp.idpapi.permission.service;

import java.util.List;

import com.idp.idpapi.permission.dto.request.PermissionUpsertRequest;
import com.idp.idpapi.permission.dto.response.PermissionResponse;

public interface PermissionService {

    List<PermissionResponse> getAll();

    PermissionResponse getById(Integer permissionId);

    PermissionResponse create(PermissionUpsertRequest request);

    PermissionResponse update(Integer permissionId, PermissionUpsertRequest request);

    void delete(Integer permissionId);
}
