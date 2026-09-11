package com.idp.idpapi.department.controller;

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
import com.idp.idpapi.department.dto.request.DepartmentUpsertRequest;
import com.idp.idpapi.department.dto.response.DepartmentResponse;
import com.idp.idpapi.department.service.DepartmentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach phong ban thanh cong.",
                departmentService.getAll()));
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('DEPARTMENT_VIEW')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable Integer departmentId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay chi tiet phong ban thanh cong.",
                departmentService.getById(departmentId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_MANAGE')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody DepartmentUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao phong ban thanh cong.",
                departmentService.create(request)));
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('DEPARTMENT_MANAGE')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable Integer departmentId,
            @Valid @RequestBody DepartmentUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat phong ban thanh cong.",
                departmentService.update(departmentId, request)));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('DEPARTMENT_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer departmentId) {
        departmentService.delete(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Xoa phong ban thanh cong."));
    }
}
