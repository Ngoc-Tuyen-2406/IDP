package com.idp.idpapi.contract.service;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.contract.dto.request.ContractCreateRequest;
import com.idp.idpapi.contract.dto.request.ContractUpdateRequest;
import com.idp.idpapi.contract.dto.request.ContractUploadRequest;
import com.idp.idpapi.contract.dto.response.ContractDetailResponse;
import com.idp.idpapi.contract.dto.response.ContractDownloadPayload;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.contract.enums.ContractStatus;

public interface ContractService {

    PageResponse<ContractSummaryResponse> getContracts(
            String keyword,
            Integer partnerId,
            Integer documentTypeId,
            ContractStatus status,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            LocalDate expiredFrom,
            LocalDate expiredTo,
            Integer currentUserId,
            Pageable pageable);

    ContractDetailResponse getById(Integer contractId, Integer currentUserId);

    ContractDetailResponse create(ContractCreateRequest request, Integer currentUserId);

    ContractDetailResponse upload(ContractUploadRequest request, MultipartFile file, Integer currentUserId);

    ContractDetailResponse update(Integer contractId, ContractUpdateRequest request, Integer currentUserId);

    void delete(Integer contractId);

    PageResponse<ContractSummaryResponse> getFavoriteContracts(Integer currentUserId, Pageable pageable);

    void markFavorite(Integer contractId, Integer currentUserId);

    void unmarkFavorite(Integer contractId, Integer currentUserId);

    ContractDownloadPayload downloadLatestFile(Integer contractId);
}
