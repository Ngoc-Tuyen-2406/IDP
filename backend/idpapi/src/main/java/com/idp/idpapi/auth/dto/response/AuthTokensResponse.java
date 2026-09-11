package com.idp.idpapi.auth.dto.response;

import com.idp.idpapi.user.dto.response.UserProfileResponse;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserProfileResponse user) {
}
