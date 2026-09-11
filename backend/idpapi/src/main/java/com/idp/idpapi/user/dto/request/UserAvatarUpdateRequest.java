package com.idp.idpapi.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserAvatarUpdateRequest(
        @NotBlank(message = "avatar khong duoc de trong.")
        String avatar) {
}
