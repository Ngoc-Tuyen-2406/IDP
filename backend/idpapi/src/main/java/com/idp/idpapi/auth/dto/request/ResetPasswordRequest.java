package com.idp.idpapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Token đặt lại mật khẩu là bắt buộc.")
        String token,

        @NotBlank(message = "Mật khẩu mới là bắt buộc.")
        String newPassword,

        @NotBlank(message = "Mật khẩu xác nhận là bắt buộc.")
        String confirmPassword) {
}
