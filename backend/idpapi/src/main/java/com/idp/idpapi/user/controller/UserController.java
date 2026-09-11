package com.idp.idpapi.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.security.SecurityUserDetails;
import com.idp.idpapi.user.dto.request.UserAvatarUpdateRequest;
import com.idp.idpapi.user.dto.request.UserCreateRequest;
import com.idp.idpapi.user.dto.request.UserRoleAssignRequest;
import com.idp.idpapi.user.dto.request.UserStatusUpdateRequest;
import com.idp.idpapi.user.dto.request.UserUpdateRequest;
import com.idp.idpapi.user.dto.response.UserResponse;
import com.idp.idpapi.user.entity.UserStatus;
import com.idp.idpapi.user.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach nguoi dung thanh cong.",
                userService.getAll(keyword, departmentId, status, page, size)));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay chi tiet nguoi dung thanh cong.",
                userService.getById(userId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao nguoi dung thanh cong.",
                userService.create(request, extractUserId(currentUser))));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat nguoi dung thanh cong.",
                userService.update(userId, request)));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer userId) {
        userService.delete(userId);
        return ResponseEntity.ok(ApiResponse.success("Xoa nguoi dung thanh cong."));
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat trang thai nguoi dung thanh cong.",
                userService.updateStatus(userId, request)));
    }

    @PutMapping("/{userId}/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @PathVariable Integer userId,
            @Valid @RequestBody UserAvatarUpdateRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        Integer currentUserId = extractUserId(currentUser);
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("Ban khong co quyen cap nhat avatar cua nguoi dung khac.");
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat avatar thanh cong.",
                userService.updateAvatar(userId, request)));
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<List<String>>> getRoles(@PathVariable Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach role cua nguoi dung thanh cong.",
                userService.getRoleNames(userId)));
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @PathVariable Integer userId,
            @Valid @RequestBody UserRoleAssignRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Gan role cho nguoi dung thanh cong.",
                userService.assignRoles(userId, request, extractUserId(currentUser))));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> revokeRole(
            @PathVariable Integer userId,
            @PathVariable Integer roleId) {
        userService.revokeRole(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Thu hoi role cua nguoi dung thanh cong."));
    }

    private Integer extractUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phien dang nhap khong hop le.");
        }
        return currentUser.getUserId();
    }
}
