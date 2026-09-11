package com.idp.idpapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu hiện tại là bắt buộc.")
        String currentPassword,

        @NotBlank(message = "Mật khẩu mới là bắt buộc.")
        String newPassword,

        @NotBlank(message = "Mật khẩu xác nhận là bắt buộc.")
        String confirmPassword) {
}
