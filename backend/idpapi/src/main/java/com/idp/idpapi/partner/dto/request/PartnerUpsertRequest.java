package com.idp.idpapi.partner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PartnerUpsertRequest(
        @NotBlank(message = "Tên đối tác là bắt buộc.")
        @Size(max = 255, message = "Tên đối tác không được vượt quá 255 ký tự.")
        String companyName,

        @Size(max = 50, message = "Loại đối tác không được vượt quá 50 ký tự.")
        String partnerType,

        @Size(max = 50, message = "Mã số thuế không được vượt quá 50 ký tự.")
        String taxCode,

        @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự.")
        String phone,

        @Email(message = "Email đối tác không hợp lệ.")
        String email,

        @Size(max = 1000, message = "Địa chỉ không được vượt quá 1000 ký tự.")
        String address,

        @Size(max = 255, message = "Website không được vượt quá 255 ký tự.")
        String website) {
}
