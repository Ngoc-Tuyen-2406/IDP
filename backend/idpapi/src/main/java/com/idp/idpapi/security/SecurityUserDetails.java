package com.idp.idpapi.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.idp.idpapi.user.entity.UserStatus;

public class SecurityUserDetails implements UserDetails {

    private final Integer userId;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final UserStatus status;
    private final boolean emailVerified;
    private final boolean deleted;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUserDetails(
            Integer userId,
            String email,
            String passwordHash,
            String fullName,
            UserStatus status,
            boolean emailVerified,
            boolean deleted,
            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.status = status;
        this.emailVerified = emailVerified;
        this.deleted = deleted;
        this.authorities = authorities;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !deleted && status == UserStatus.ACTIVE && emailVerified;
    }
}
