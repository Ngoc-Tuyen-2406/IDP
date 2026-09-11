package com.idp.idpapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Token xác thực là bắt buộc.")
        String token) {
}
