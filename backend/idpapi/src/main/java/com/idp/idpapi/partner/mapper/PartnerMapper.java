package com.idp.idpapi.partner.mapper;

import org.springframework.stereotype.Component;

import com.idp.idpapi.partner.dto.request.PartnerUpsertRequest;
import com.idp.idpapi.partner.dto.response.PartnerResponse;
import com.idp.idpapi.partner.entity.Partner;

@Component
public class PartnerMapper {

    public PartnerResponse toResponse(Partner entity) {
        return new PartnerResponse(
                entity.getPartnerId(),
                entity.getCompanyName(),
                entity.getPartnerType(),
                entity.getTaxCode(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getWebsite(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public void updateEntity(Partner entity, PartnerUpsertRequest request) {
        entity.setCompanyName(request.companyName().trim());
        entity.setPartnerType(request.partnerType());
        entity.setTaxCode(request.taxCode());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setAddress(request.address());
        entity.setWebsite(request.website());
    }
}
