package com.idp.idpapi.permission.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionUpsertRequest(
        @NotBlank(message = "permissionName khong duoc de trong.")
        @Size(max = 100, message = "permissionName khong duoc vuot qua 100 ky tu.")
        String permissionName,

        @Size(max = 2000, message = "description khong duoc vuot qua 2000 ky tu.")
        String description) {
}
