package com.idp.idpapi.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.auth.constants.AuthMessages;
import com.idp.idpapi.auth.dto.request.ChangePasswordRequest;
import com.idp.idpapi.auth.dto.request.ForgotPasswordRequest;
import com.idp.idpapi.auth.dto.request.LoginRequest;
import com.idp.idpapi.auth.dto.request.LogoutRequest;
import com.idp.idpapi.auth.dto.request.RefreshTokenRequest;
import com.idp.idpapi.auth.dto.request.ResendVerificationRequest;
import com.idp.idpapi.auth.dto.request.ResetPasswordRequest;
import com.idp.idpapi.auth.dto.request.VerifyEmailRequest;
import com.idp.idpapi.auth.dto.response.AuthTokensResponse;
import com.idp.idpapi.auth.dto.response.UserSessionResponse;
import com.idp.idpapi.auth.service.AuthService;
import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.common.util.RequestUtils;
import com.idp.idpapi.security.SecurityUserDetails;
import com.idp.idpapi.user.dto.response.UserProfileResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {
        AuthTokensResponse response = authService.login(
                request,
                RequestUtils.resolveClientIp(httpServletRequest),
                RequestUtils.resolveDeviceName(httpServletRequest));
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.LOGIN_SUCCESS, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody LogoutRequest request) {
        authService.logout(extractUserId(currentUser), request);
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.LOGOUT_SUCCESS));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        authService.logoutAll(extractUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.LOGOUT_ALL_SUCCESS));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest) {
        AuthTokensResponse response = authService.refreshToken(
                request,
                RequestUtils.resolveClientIp(httpServletRequest),
                RequestUtils.resolveDeviceName(httpServletRequest));
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.REFRESH_SUCCESS, response));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> profile(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        UserProfileResponse profile = authService.getProfile(extractUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.PROFILE_LOADED, profile));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(extractUserId(currentUser), request);
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.PASSWORD_CHANGED));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.PASSWORD_RESET_REQUESTED));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.PASSWORD_RESET_SUCCESS));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.EMAIL_VERIFIED));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.EMAIL_VERIFICATION_SENT));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<PageResponse<UserSessionResponse>>> sessions(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0.") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size phải >= 1.")
            @Max(value = 100, message = "size không được vượt quá 100.") int size) {
        PageResponse<UserSessionResponse> sessions = authService.getSessions(extractUserId(currentUser), page, size);
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.SESSIONS_LOADED, sessions));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @PathVariable Integer sessionId) {
        authService.revokeSession(extractUserId(currentUser), sessionId);
        return ResponseEntity.ok(ApiResponse.success(AuthMessages.SESSION_REVOKED));
    }

    private Integer extractUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phiên đăng nhập không hợp lệ.");
        }
        return currentUser.getUserId();
    }
}
