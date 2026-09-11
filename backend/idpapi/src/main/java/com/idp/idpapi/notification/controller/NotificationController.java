package com.idp.idpapi.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.notification.dto.request.NotificationSettingsUpdateRequest;
import com.idp.idpapi.notification.dto.response.NotificationResponse;
import com.idp.idpapi.notification.dto.response.NotificationSettingsResponse;
import com.idp.idpapi.notification.service.NotificationService;
import com.idp.idpapi.security.SecurityUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Notifications")
@PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach thong bao thanh cong.",
                notificationService.getNotifications(extractUserId(currentUser), page, size)));
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Integer notificationId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        notificationService.markRead(notificationId, extractUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Danh dau da doc thanh cong."));
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        notificationService.markAllRead(extractUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Danh dau toan bo thong bao da doc thanh cong."));
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer notificationId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        notificationService.delete(notificationId, extractUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Xoa thong bao thanh cong."));
    }

    @DeleteMapping("/notifications")
    public ResponseEntity<ApiResponse<Void>> deleteAll(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        notificationService.deleteAll(extractUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Xoa toan bo thong bao thanh cong."));
    }

    @GetMapping("/notification-settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getSettings(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay cai dat thong bao thanh cong.",
                notificationService.getSettings(extractUserId(currentUser))));
    }

    @PutMapping("/notification-settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> updateSettings(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody NotificationSettingsUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat cai dat thong bao thanh cong.",
                notificationService.updateSettings(extractUserId(currentUser), request)));
    }

    private Integer extractUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phien dang nhap khong hop le.");
        }
        return currentUser.getUserId();
    }
}
