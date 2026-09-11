package com.idp.idpapi.partner.dto.response;

import java.time.LocalDateTime;

public record PartnerResponse(
        Integer partnerId,
        String companyName,
        String partnerType,
        String taxCode,
        String phone,
        String email,
        String address,
        String website,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
