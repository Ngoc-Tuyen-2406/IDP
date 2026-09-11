package com.idp.idpapi.user.dto.request;

import java.util.Set;

import com.idp.idpapi.user.entity.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        Integer departmentId,

        @NotBlank(message = "fullName khong duoc de trong.")
        @Size(max = 255, message = "fullName khong duoc vuot qua 255 ky tu.")
        String fullName,

        @NotBlank(message = "email khong duoc de trong.")
        @Email(message = "email khong hop le.")
        @Size(max = 255, message = "email khong duoc vuot qua 255 ky tu.")
        String email,

        @NotBlank(message = "password khong duoc de trong.")
        @Size(min = 8, max = 255, message = "password phai tu 8 den 255 ky tu.")
        String password,

        @Size(max = 20, message = "phone khong duoc vuot qua 20 ky tu.")
        String phone,

        String avatar,

        UserStatus status,

        Boolean emailVerified,

        @NotEmpty(message = "roleIds khong duoc de trong.")
        Set<@NotNull(message = "roleId khong hop le.") Integer> roleIds) {
}
