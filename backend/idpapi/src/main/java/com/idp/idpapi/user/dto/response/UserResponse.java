package com.idp.idpapi.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Integer userId,
        Integer departmentId,
        String departmentName,
        String fullName,
        String email,
        String phone,
        String avatar,
        String status,
        Boolean emailVerified,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> roles) {
}
