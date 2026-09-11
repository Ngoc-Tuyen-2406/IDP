package com.idp.idpapi.permission.controller;

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
import com.idp.idpapi.permission.dto.request.PermissionUpsertRequest;
import com.idp.idpapi.permission.dto.response.PermissionResponse;
import com.idp.idpapi.permission.service.PermissionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach permission thanh cong.",
                permissionService.getAll()));
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getById(@PathVariable Integer permissionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay chi tiet permission thanh cong.",
                permissionService.getById(permissionId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> create(
            @Valid @RequestBody PermissionUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao permission thanh cong.",
                permissionService.create(request)));
    }

    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @PathVariable Integer permissionId,
            @Valid @RequestBody PermissionUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat permission thanh cong.",
                permissionService.update(permissionId, request)));
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer permissionId) {
        permissionService.delete(permissionId);
        return ResponseEntity.ok(ApiResponse.success("Xoa permission thanh cong."));
    }
}
