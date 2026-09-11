package com.idp.idpapi.partner.service;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.partner.dto.request.PartnerUpsertRequest;
import com.idp.idpapi.partner.dto.response.PartnerResponse;

public interface PartnerService {

    PageResponse<PartnerResponse> getAll(String keyword, int page, int size);

    PartnerResponse getById(Integer partnerId);

    PartnerResponse create(PartnerUpsertRequest request);

    PartnerResponse update(Integer partnerId, PartnerUpsertRequest request);

    void delete(Integer partnerId);
}
