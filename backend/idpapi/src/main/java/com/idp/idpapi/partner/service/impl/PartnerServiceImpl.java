package com.idp.idpapi.partner.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.partner.dto.request.PartnerUpsertRequest;
import com.idp.idpapi.partner.dto.response.PartnerResponse;
import com.idp.idpapi.partner.entity.Partner;
import com.idp.idpapi.partner.mapper.PartnerMapper;
import com.idp.idpapi.partner.repository.PartnerRepository;
import com.idp.idpapi.partner.service.PartnerService;

@Service
@Transactional
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerMapper partnerMapper;

    public PartnerServiceImpl(PartnerRepository partnerRepository, PartnerMapper partnerMapper) {
        this.partnerRepository = partnerRepository;
        this.partnerMapper = partnerMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PartnerResponse> getAll(String keyword, int page, int size) {
        Page<Partner> partners;
        if (StringUtils.hasText(keyword)) {
            partners = partnerRepository.findByCompanyNameContainingIgnoreCaseOrTaxCodeContainingIgnoreCase(
                    keyword.trim(),
                    keyword.trim(),
                    PageRequest.of(page, size));
        } else {
            partners = partnerRepository.findAll(PageRequest.of(page, size));
        }
        return PageResponse.from(partners.map(partnerMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerResponse getById(Integer partnerId) {
        return partnerMapper.toResponse(getEntity(partnerId));
    }

    @Override
    public PartnerResponse create(PartnerUpsertRequest request) {
        validateTaxCodeForCreate(request.taxCode());
        Partner partner = new Partner();
        partnerMapper.updateEntity(partner, request);
        return partnerMapper.toResponse(partnerRepository.save(partner));
    }

    @Override
    public PartnerResponse update(Integer partnerId, PartnerUpsertRequest request) {
        Partner partner = getEntity(partnerId);
        validateTaxCodeForUpdate(request.taxCode(), partnerId);
        partnerMapper.updateEntity(partner, request);
        return partnerMapper.toResponse(partnerRepository.save(partner));
    }

    @Override
    public void delete(Integer partnerId) {
        partnerRepository.delete(getEntity(partnerId));
    }

    private Partner getEntity(Integer partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đối tác."));
    }

    private void validateTaxCodeForCreate(String taxCode) {
        if (StringUtils.hasText(taxCode) && partnerRepository.existsByTaxCodeIgnoreCase(taxCode.trim())) {
            throw new BadRequestException("Mã số thuế đã tồn tại.");
        }
    }

    private void validateTaxCodeForUpdate(String taxCode, Integer partnerId) {
        if (StringUtils.hasText(taxCode)
                && partnerRepository.existsByTaxCodeIgnoreCaseAndPartnerIdNot(taxCode.trim(), partnerId)) {
            throw new BadRequestException("Mã số thuế đã tồn tại.");
        }
    }
}
