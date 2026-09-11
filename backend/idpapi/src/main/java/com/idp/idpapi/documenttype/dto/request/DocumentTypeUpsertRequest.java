package com.idp.idpapi.documenttype.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentTypeUpsertRequest(
        @NotBlank(message = "Tên loại tài liệu là bắt buộc.")
        @Size(max = 100, message = "Tên loại tài liệu không được vượt quá 100 ký tự.")
        String name,

        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự.")
        String description) {
}
