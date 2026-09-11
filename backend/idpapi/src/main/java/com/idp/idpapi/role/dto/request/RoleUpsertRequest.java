package com.idp.idpapi.role.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleUpsertRequest(
        @NotBlank(message = "roleName khong duoc de trong.")
        @Size(max = 50, message = "roleName khong duoc vuot qua 50 ky tu.")
        String roleName,

        @Size(max = 2000, message = "description khong duoc vuot qua 2000 ky tu.")
        String description) {
}
