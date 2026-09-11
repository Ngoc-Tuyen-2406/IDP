package com.idp.idpapi.auth.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import com.idp.idpapi.auth.entity.EmailVerificationToken;
import com.idp.idpapi.auth.entity.PasswordResetToken;
import com.idp.idpapi.auth.entity.RefreshToken;
import com.idp.idpapi.auth.exception.AuthenticationFailedException;
import com.idp.idpapi.auth.exception.TokenValidationException;
import com.idp.idpapi.auth.mapper.AuthMapper;
import com.idp.idpapi.auth.repository.EmailVerificationTokenRepository;
import com.idp.idpapi.auth.repository.PasswordResetTokenRepository;
import com.idp.idpapi.auth.repository.RefreshTokenRepository;
import com.idp.idpapi.auth.service.AuthMailService;
import com.idp.idpapi.auth.service.AuthService;
import com.idp.idpapi.auth.validator.PasswordPolicyValidator;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.common.util.HashingUtils;
import com.idp.idpapi.common.util.SecureTokenGenerator;
import com.idp.idpapi.security.JwtTokenService;
import com.idp.idpapi.security.SecurityProperties;
import com.idp.idpapi.user.dto.response.UserProfileResponse;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.entity.UserStatus;
import com.idp.idpapi.user.repository.UserRepository;
import com.idp.idpapi.user.repository.UserRoleRepository;
import com.idp.idpapi.role.repository.RolePermissionRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final SecurityProperties securityProperties;
    private final Clock clock;
    private final AuthMailService authMailService;
    private final AuthMapper authMapper;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public AuthServiceImpl(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            SecurityProperties securityProperties,
            Clock clock,
            AuthMailService authMailService,
            AuthMapper authMapper,
            PasswordPolicyValidator passwordPolicyValidator) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.securityProperties = securityProperties;
        this.clock = clock;
        this.authMailService = authMailService;
        this.authMapper = authMapper;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Override
    public AuthTokensResponse login(LoginRequest request, String ipAddress, String deviceName) {
        User user = userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(request.email().trim())
                .orElseThrow(() -> new AuthenticationFailedException(AuthMessages.INVALID_CREDENTIALS));

        LocalDateTime now = LocalDateTime.now(clock);
        unlockAccountIfLockExpired(user, now);
        assertUserCanAuthenticate(user, now);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailedLogin(user, now);
            throw new AuthenticationFailedException(AuthMessages.INVALID_CREDENTIALS);
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLogin(now);
        userRepository.save(user);

        return issueTokens(user, ipAddress, deviceName, now);
    }

    @Override
    public void logout(Integer userId, LogoutRequest request) {
        String hashedToken = HashingUtils.sha256(request.refreshToken());
        refreshTokenRepository.findByToken(hashedToken).ifPresent(refreshToken -> {
            if (!refreshToken.getUser().getUserId().equals(userId)) {
                throw new UnauthorizedException(AuthMessages.INVALID_REFRESH_TOKEN);
            }
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Override
    public void logoutAll(Integer userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    public AuthTokensResponse refreshToken(RefreshTokenRequest request, String ipAddress, String deviceName) {
        LocalDateTime now = LocalDateTime.now(clock);
        String hashedToken = HashingUtils.sha256(request.refreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new AuthenticationFailedException(AuthMessages.INVALID_REFRESH_TOKEN));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())
                || (refreshToken.getExpiresAt() != null && !refreshToken.getExpiresAt().isAfter(now))) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new AuthenticationFailedException(AuthMessages.INVALID_REFRESH_TOKEN);
        }

        User user = refreshToken.getUser();
        unlockAccountIfLockExpired(user, now);
        assertUserCanAuthenticate(user, now);

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        userRepository.save(user);

        return issueTokens(user, ipAddress, deviceName, now);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Integer userId) {
        User user = getManagedUser(userId);
        return buildUserProfile(user);
    }

    @Override
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = getManagedUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException(AuthMessages.CURRENT_PASSWORD_INCORRECT);
        }

        assertPasswordConfirmation(request.newPassword(), request.confirmPassword());
        passwordPolicyValidator.validate(request.newPassword());

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now(clock));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(request.email().trim()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserUserIdAndUsedFalse(user.getUserId());

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            String rawToken = SecureTokenGenerator.generate();
            resetToken.setToken(HashingUtils.sha256(rawToken));
            resetToken.setExpiresAt(LocalDateTime.now(clock)
                    .plusMinutes(securityProperties.getPasswordResetExpirationMinutes()));
            passwordResetTokenRepository.save(resetToken);
            authMailService.sendPasswordReset(user, rawToken);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        assertPasswordConfirmation(request.newPassword(), request.confirmPassword());
        passwordPolicyValidator.validate(request.newPassword());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(
                HashingUtils.sha256(request.token()))
                .orElseThrow(() -> new TokenValidationException(AuthMessages.INVALID_RESET_TOKEN));

        LocalDateTime now = LocalDateTime.now(clock);
        if (!resetToken.getExpiresAt().isAfter(now)) {
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            throw new TokenValidationException(AuthMessages.INVALID_RESET_TOKEN);
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(now);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED && Boolean.TRUE.equals(user.getEmailVerified())) {
            user.setStatus(UserStatus.ACTIVE);
        }

        resetToken.setUsed(true);
        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.revokeAllByUserId(user.getUserId());
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(
                HashingUtils.sha256(request.token()))
                .orElseThrow(() -> new TokenValidationException(AuthMessages.INVALID_VERIFY_TOKEN));

        LocalDateTime now = LocalDateTime.now(clock);
        if (verificationToken.getVerifiedAt() != null || !verificationToken.getExpiresAt().isAfter(now)) {
            throw new TokenValidationException(AuthMessages.INVALID_VERIFY_TOKEN);
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        if (user.getStatus() == UserStatus.PENDING || user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }

        verificationToken.setVerifiedAt(now);
        userRepository.save(user);
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Override
    public void resendVerification(ResendVerificationRequest request) {
        userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(request.email().trim()).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                return;
            }

            emailVerificationTokenRepository.deleteByUserUserIdAndVerifiedAtIsNull(user.getUserId());

            EmailVerificationToken verificationToken = new EmailVerificationToken();
            verificationToken.setUser(user);
            String rawToken = SecureTokenGenerator.generate();
            verificationToken.setToken(HashingUtils.sha256(rawToken));
            verificationToken.setExpiresAt(LocalDateTime.now(clock)
                    .plusHours(securityProperties.getEmailVerificationExpirationHours()));
            emailVerificationTokenRepository.save(verificationToken);
            authMailService.sendVerificationEmail(user, rawToken);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserSessionResponse> getSessions(Integer userId, int page, int size) {
        Page<RefreshToken> sessions = refreshTokenRepository.findActiveSessions(
                userId,
                LocalDateTime.now(clock),
                PageRequest.of(page, size));

        return PageResponse.from(sessions.map(token -> authMapper.toSessionResponse(token, LocalDateTime.now(clock))));
    }

    @Override
    public void revokeSession(Integer userId, Integer sessionId) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenIdAndUserUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đăng nhập."));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    private AuthTokensResponse issueTokens(User user, String ipAddress, String deviceName, LocalDateTime currentTime) {
        List<String> roles = getRoleNames(user.getUserId());
        Set<String> permissions = getPermissionNames(user.getUserId());

        String accessToken = jwtTokenService.generateAccessToken(user.getUserId(), user.getEmail(), roles, permissions);
        String rawRefreshToken = SecureTokenGenerator.generate();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(HashingUtils.sha256(rawRefreshToken));
        refreshToken.setExpiresAt(currentTime.plusDays(securityProperties.getRefreshTokenExpirationDays()));
        refreshToken.setDeviceName(StringUtils.hasText(deviceName) ? deviceName : "unknown-device");
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setCreatedByIp(ipAddress);
        refreshTokenRepository.save(refreshToken);

        return new AuthTokensResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                securityProperties.getAccessTokenExpirationMinutes() * 60,
                authMapper.toUserProfile(user, roles, permissions));
    }

    private UserProfileResponse buildUserProfile(User user) {
        List<String> roles = getRoleNames(user.getUserId());
        Set<String> permissions = getPermissionNames(user.getUserId());
        return authMapper.toUserProfile(user, roles, permissions);
    }

    private List<String> getRoleNames(Integer userId) {
        return userRoleRepository.findRoleNamesByUserId(userId);
    }

    private Set<String> getPermissionNames(Integer userId) {
        List<Integer> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        return roleIds.isEmpty() ? Set.of() : rolePermissionRepository.findPermissionNamesByRoleIds(roleIds);
    }

    private User getManagedUser(Integer userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));
    }

    private void unlockAccountIfLockExpired(User user, LocalDateTime currentTime) {
        if (user.getStatus() == UserStatus.LOCKED
                && user.getLockedUntil() != null
                && !user.getLockedUntil().isAfter(currentTime)) {
            user.setLockedUntil(null);
            user.setFailedLoginCount(0);
            user.setStatus(Boolean.TRUE.equals(user.getEmailVerified()) ? UserStatus.ACTIVE : UserStatus.PENDING);
        }
    }

    private void assertUserCanAuthenticate(User user, LocalDateTime currentTime) {
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new AuthenticationFailedException(AuthMessages.INVALID_CREDENTIALS);
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(currentTime)) {
            user.setStatus(UserStatus.LOCKED);
            throw new AuthenticationFailedException(AuthMessages.ACCOUNT_LOCKED);
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified()) || user.getStatus() == UserStatus.PENDING) {
            throw new AuthenticationFailedException(AuthMessages.EMAIL_NOT_VERIFIED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AuthenticationFailedException(AuthMessages.ACCOUNT_INACTIVE);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AuthenticationFailedException(AuthMessages.ACCOUNT_LOCKED);
        }
    }

    private void registerFailedLogin(User user, LocalDateTime currentTime) {
        int failedLoginCount = user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount();
        failedLoginCount++;
        user.setFailedLoginCount(failedLoginCount);

        if (failedLoginCount >= securityProperties.getMaxFailedLoginAttempts()) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(currentTime.plusMinutes(securityProperties.getAccountLockMinutes()));
        }

        userRepository.save(user);
    }

    private void assertPasswordConfirmation(String newPassword, String confirmPassword) {
        if (!StringUtils.hasText(newPassword) || !newPassword.equals(confirmPassword)) {
            throw new BadRequestException(AuthMessages.PASSWORD_CONFIRM_MISMATCH);
        }
    }
}
