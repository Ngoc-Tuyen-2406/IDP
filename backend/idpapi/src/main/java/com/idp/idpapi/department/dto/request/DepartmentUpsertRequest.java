package com.idp.idpapi.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentUpsertRequest(
        @NotBlank(message = "departmentName khong duoc de trong.")
        @Size(max = 100, message = "departmentName khong duoc vuot qua 100 ky tu.")
        String departmentName,

        @Size(max = 2000, message = "description khong duoc vuot qua 2000 ky tu.")
        String description) {
}
