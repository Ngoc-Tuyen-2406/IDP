package com.idp.idpapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    @NotBlank(message = "APP_SECURITY_JWT_SECRET is required.")
    @Size(min = 32, message = "JWT secret must contain at least 32 characters.")
    private String jwtSecret;

    @Min(value = 1, message = "Access token expiration must be greater than zero.")
    private long accessTokenExpirationMinutes = 30;

    @Min(value = 1, message = "Refresh token expiration must be greater than zero.")
    private long refreshTokenExpirationDays = 7;

    @Min(value = 1, message = "Password reset token expiration must be greater than zero.")
    private long passwordResetExpirationMinutes = 30;

    @Min(value = 1, message = "Email verification token expiration must be greater than zero.")
    private long emailVerificationExpirationHours = 24;

    @Min(value = 1, message = "Max failed login attempts must be greater than zero.")
    private int maxFailedLoginAttempts = 5;

    @Min(value = 1, message = "Account lock duration must be greater than zero.")
    private long accountLockMinutes = 15;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public void setAccessTokenExpirationMinutes(long accessTokenExpirationMinutes) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
    }

    public long getRefreshTokenExpirationDays() {
        return refreshTokenExpirationDays;
    }

    public void setRefreshTokenExpirationDays(long refreshTokenExpirationDays) {
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public long getPasswordResetExpirationMinutes() {
        return passwordResetExpirationMinutes;
    }

    public void setPasswordResetExpirationMinutes(long passwordResetExpirationMinutes) {
        this.passwordResetExpirationMinutes = passwordResetExpirationMinutes;
    }

    public long getEmailVerificationExpirationHours() {
        return emailVerificationExpirationHours;
    }

    public void setEmailVerificationExpirationHours(long emailVerificationExpirationHours) {
        this.emailVerificationExpirationHours = emailVerificationExpirationHours;
    }

    public int getMaxFailedLoginAttempts() {
        return maxFailedLoginAttempts;
    }

    public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
        this.maxFailedLoginAttempts = maxFailedLoginAttempts;
    }

    public long getAccountLockMinutes() {
        return accountLockMinutes;
    }

    public void setAccountLockMinutes(long accountLockMinutes) {
        this.accountLockMinutes = accountLockMinutes;
    }
}
