package com.idp.idpapi.auth.dto.response;

import java.time.LocalDateTime;

public record UserSessionResponse(
        Integer sessionId,
        String deviceName,
        String ipAddress,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        boolean revoked,
        boolean expired) {
}
