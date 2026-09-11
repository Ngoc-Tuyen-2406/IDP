package com.idp.idpapi.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
        Integer userId,
        String fullName,
        String email,
        String phone,
        String avatar,
        Integer departmentId,
        String departmentName,
        String status,
        boolean emailVerified,
        LocalDateTime lastLogin,
        List<String> roles,
        List<String> permissions) {
}
