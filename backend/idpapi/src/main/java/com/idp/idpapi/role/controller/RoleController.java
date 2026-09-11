package com.idp.idpapi.role.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.permission.dto.response.PermissionResponse;
import com.idp.idpapi.role.dto.request.RolePermissionAssignRequest;
import com.idp.idpapi.role.dto.request.RoleUpsertRequest;
import com.idp.idpapi.role.dto.response.RoleDetailResponse;
import com.idp.idpapi.role.dto.response.RoleResponse;
import com.idp.idpapi.role.service.RoleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach role thanh cong.",
                roleService.getAll()));
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> getById(@PathVariable Integer roleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay chi tiet role thanh cong.",
                roleService.getById(roleId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao role thanh cong.",
                roleService.create(request)));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable Integer roleId,
            @Valid @RequestBody RoleUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat role thanh cong.",
                roleService.update(roleId, request)));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer roleId) {
        roleService.delete(roleId);
        return ResponseEntity.ok(ApiResponse.success("Xoa role thanh cong."));
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions(@PathVariable Integer roleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach permission cua role thanh cong.",
                roleService.getPermissions(roleId)));
    }

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> assignPermissions(
            @PathVariable Integer roleId,
            @Valid @RequestBody RolePermissionAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Gan permission cho role thanh cong.",
                roleService.assignPermissions(roleId, request)));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @PathVariable Integer roleId,
            @PathVariable Integer permissionId) {
        roleService.revokePermission(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Thu hoi permission khoi role thanh cong."));
    }
}
