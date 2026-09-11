package com.idp.idpapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "Refresh token là bắt buộc.")
        String refreshToken) {
}
