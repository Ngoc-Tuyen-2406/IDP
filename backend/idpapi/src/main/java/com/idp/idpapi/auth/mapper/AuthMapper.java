package com.idp.idpapi.auth.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.idp.idpapi.auth.dto.response.UserSessionResponse;
import com.idp.idpapi.auth.entity.RefreshToken;
import com.idp.idpapi.user.dto.response.UserProfileResponse;
import com.idp.idpapi.user.entity.User;

@Component
public class AuthMapper {

    public UserProfileResponse toUserProfile(User user, List<String> roles, Set<String> permissions) {
        Integer departmentId = user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null;
        String departmentName = user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null;

        return new UserProfileResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                departmentId,
                departmentName,
                user.getStatus().name(),
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getLastLogin(),
                roles.stream().distinct().sorted().toList(),
                permissions.stream().sorted().toList());
    }

    public UserSessionResponse toSessionResponse(RefreshToken refreshToken, LocalDateTime currentTime) {
        boolean expired = refreshToken.getExpiresAt() != null && !refreshToken.getExpiresAt().isAfter(currentTime);

        return new UserSessionResponse(
                refreshToken.getTokenId(),
                refreshToken.getDeviceName(),
                refreshToken.getIpAddress(),
                refreshToken.getCreatedAt(),
                refreshToken.getExpiresAt(),
                Boolean.TRUE.equals(refreshToken.getRevoked()),
                expired);
    }
}
