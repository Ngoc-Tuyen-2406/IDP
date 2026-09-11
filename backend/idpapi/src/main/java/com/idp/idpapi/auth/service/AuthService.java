package com.idp.idpapi.auth.service;

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
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.user.dto.response.UserProfileResponse;

public interface AuthService {

    AuthTokensResponse login(LoginRequest request, String ipAddress, String deviceName);

    void logout(Integer userId, LogoutRequest request);

    void logoutAll(Integer userId);

    AuthTokensResponse refreshToken(RefreshTokenRequest request, String ipAddress, String deviceName);

    UserProfileResponse getProfile(Integer userId);

    void changePassword(Integer userId, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerification(ResendVerificationRequest request);

    PageResponse<UserSessionResponse> getSessions(Integer userId, int page, int size);

    void revokeSession(Integer userId, Integer sessionId);
}
