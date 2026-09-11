package com.idp.idpapi.user.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.idp.idpapi.user.dto.request.UserCreateRequest;
import com.idp.idpapi.user.dto.request.UserUpdateRequest;
import com.idp.idpapi.user.dto.response.UserResponse;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.entity.UserStatus;

@Component
public class UserMapper {

    public void applyCreateRequest(User user, UserCreateRequest request) {
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhone(request.phone());
        user.setAvatar(request.avatar());
        user.setStatus(request.status() != null ? request.status() : UserStatus.PENDING);
        user.setEmailVerified(Boolean.TRUE.equals(request.emailVerified()));
    }

    public void applyUpdateRequest(User user, UserUpdateRequest request) {
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhone(request.phone());
        user.setAvatar(request.avatar());
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        if (request.emailVerified() != null) {
            user.setEmailVerified(request.emailVerified());
        }
    }

    public UserResponse toResponse(User user, List<String> roles) {
        return new UserResponse(
                user.getUserId(),
                user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null,
                user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null,
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus() != null ? user.getStatus().name() : null,
                user.getEmailVerified(),
                user.getLastLogin(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                roles);
    }
}
