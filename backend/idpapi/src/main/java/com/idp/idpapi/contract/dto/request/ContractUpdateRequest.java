package com.idp.idpapi.contract.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.idp.idpapi.contract.enums.ContractStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContractUpdateRequest(
        @NotNull(message = "Loại tài liệu là bắt buộc.")
        Integer documentTypeId,

        @NotNull(message = "Đối tác là bắt buộc.")
        Integer partnerId,

        @NotBlank(message = "Số hợp đồng là bắt buộc.")
        @Size(max = 100, message = "Số hợp đồng không được vượt quá 100 ký tự.")
        String contractNumber,

        @Size(max = 255, message = "Tên hợp đồng không được vượt quá 255 ký tự.")
        String contractName,

        @Size(max = 255, message = "Tên người đại diện không được vượt quá 255 ký tự.")
        String partnerRepresentativeName,

        @Size(max = 255, message = "Chức vụ người đại diện không được vượt quá 255 ký tự.")
        String partnerRepresentativePosition,

        LocalDate signedDate,
        LocalDate effectiveDate,
        LocalDate expiredDate,
        BigDecimal totalValue,

        @Size(max = 10, message = "Mã tiền tệ không được vượt quá 10 ký tự.")
        String currency,

        ContractStatus status,

        String changeNote) {
}
