package com.idp.idpapi.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.idp.idpapi.auth.dto.request.LoginRequest;
import com.idp.idpapi.auth.dto.request.VerifyEmailRequest;
import com.idp.idpapi.auth.dto.response.AuthTokensResponse;
import com.idp.idpapi.auth.entity.EmailVerificationToken;
import com.idp.idpapi.auth.entity.RefreshToken;
import com.idp.idpapi.auth.exception.AuthenticationFailedException;
import com.idp.idpapi.auth.mapper.AuthMapper;
import com.idp.idpapi.auth.repository.EmailVerificationTokenRepository;
import com.idp.idpapi.auth.repository.PasswordResetTokenRepository;
import com.idp.idpapi.auth.repository.RefreshTokenRepository;
import com.idp.idpapi.auth.service.AuthMailService;
import com.idp.idpapi.auth.validator.PasswordPolicyValidator;
import com.idp.idpapi.role.repository.RolePermissionRepository;
import com.idp.idpapi.security.JwtTokenService;
import com.idp.idpapi.security.SecurityProperties;
import com.idp.idpapi.user.dto.response.UserProfileResponse;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.entity.UserStatus;
import com.idp.idpapi.user.repository.UserRepository;
import com.idp.idpapi.user.repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuthMailService authMailService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;

    private AuthServiceImpl authService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.setJwtSecret("12345678901234567890123456789012");
        securityProperties.setAccessTokenExpirationMinutes(30);
        securityProperties.setRefreshTokenExpirationDays(7);
        securityProperties.setPasswordResetExpirationMinutes(30);
        securityProperties.setEmailVerificationExpirationHours(24);
        securityProperties.setMaxFailedLoginAttempts(5);
        securityProperties.setAccountLockMinutes(15);

        clock = Clock.fixed(Instant.parse("2026-07-27T09:30:00Z"), ZoneOffset.UTC);

        authService = new AuthServiceImpl(
                userRepository,
                userRoleRepository,
                rolePermissionRepository,
                refreshTokenRepository,
                passwordResetTokenRepository,
                emailVerificationTokenRepository,
                passwordEncoder,
                jwtTokenService,
                securityProperties,
                clock,
                authMailService,
                authMapper,
                passwordPolicyValidator);
    }

    @Test
    void loginShouldIssueTokensForActiveUser() {
        User user = buildActiveUser();
        user.setFailedLoginCount(2);

        when(userRepository.findByEmailIgnoreCaseAndIsDeletedFalse("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRoleRepository.findRoleNamesByUserId(1)).thenReturn(List.of("Admin"));
        when(userRoleRepository.findRoleIdsByUserId(1)).thenReturn(List.of(1));
        when(rolePermissionRepository.findPermissionNamesByRoleIds(anyCollection())).thenReturn(Set.of("contracts:read"));
        when(jwtTokenService.generateAccessToken(anyInt(), anyString(), any(List.class), any(Set.class)))
                .thenReturn("jwt-access");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authMapper.toUserProfile(user, List.of("Admin"), Set.of("contracts:read")))
                .thenReturn(new UserProfileResponse(
                        1,
                        "Nguyen Van A",
                        "admin@example.com",
                        "0909000000",
                        null,
                        null,
                        null,
                        "ACTIVE",
                        true,
                        LocalDateTime.ofInstant(Instant.parse("2026-07-27T09:30:00Z"), ZoneOffset.UTC),
                        List.of("Admin"),
                        List.of("contracts:read")));

        AuthTokensResponse response = authService.login(
                new LoginRequest("admin@example.com", "Password123"),
                "127.0.0.1",
                "JUnit");

        assertEquals("jwt-access", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertNotNull(response.refreshToken());
        assertEquals(0, user.getFailedLoginCount());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getLastLogin());

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertEquals("127.0.0.1", tokenCaptor.getValue().getIpAddress());
        assertEquals("JUnit", tokenCaptor.getValue().getDeviceName());
    }

    @Test
    void loginShouldLockAccountAfterTooManyFailures() {
        User user = buildActiveUser();
        user.setFailedLoginCount(4);

        when(userRepository.findByEmailIgnoreCaseAndIsDeletedFalse("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", user.getPasswordHash())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(new LoginRequest("admin@example.com", "WrongPassword"), "127.0.0.1", "JUnit"));

        assertEquals(5, user.getFailedLoginCount());
        assertEquals(UserStatus.LOCKED, user.getStatus());
        assertEquals(LocalDateTime.ofInstant(Instant.parse("2026-07-27T09:45:00Z"), ZoneOffset.UTC), user.getLockedUntil());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void verifyEmailShouldActivatePendingUser() {
        User user = new User();
        user.setUserId(1);
        user.setEmail("pending@example.com");
        user.setFullName("Pending User");
        user.setPasswordHash("hashed");
        user.setEmailVerified(false);
        user.setStatus(UserStatus.PENDING);
        user.setIsDeleted(false);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken("hashed-token");
        token.setExpiresAt(LocalDateTime.ofInstant(Instant.parse("2026-07-28T09:30:00Z"), ZoneOffset.UTC));

        when(emailVerificationTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.verifyEmail(new VerifyEmailRequest("raw-token"));

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(true, user.getEmailVerified());
        assertEquals(LocalDateTime.ofInstant(Instant.parse("2026-07-27T09:30:00Z"), ZoneOffset.UTC), token.getVerifiedAt());
    }

    private User buildActiveUser() {
        User user = new User();
        user.setUserId(1);
        user.setEmail("admin@example.com");
        user.setFullName("Nguyen Van A");
        user.setPasswordHash("$2a$10$hashed");
        user.setPhone("0909000000");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setIsDeleted(false);
        user.setFailedLoginCount(0);
        return user;
    }
}
