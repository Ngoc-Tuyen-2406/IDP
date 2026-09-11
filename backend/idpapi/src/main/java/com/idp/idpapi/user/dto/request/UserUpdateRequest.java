package com.idp.idpapi.user.dto.request;

import com.idp.idpapi.user.entity.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        Integer departmentId,

        @NotBlank(message = "fullName khong duoc de trong.")
        @Size(max = 255, message = "fullName khong duoc vuot qua 255 ky tu.")
        String fullName,

        @NotBlank(message = "email khong duoc de trong.")
        @Email(message = "email khong hop le.")
        @Size(max = 255, message = "email khong duoc vuot qua 255 ky tu.")
        String email,

        @Size(max = 20, message = "phone khong duoc vuot qua 20 ky tu.")
        String phone,

        String avatar,

        UserStatus status,

        Boolean emailVerified) {
}
