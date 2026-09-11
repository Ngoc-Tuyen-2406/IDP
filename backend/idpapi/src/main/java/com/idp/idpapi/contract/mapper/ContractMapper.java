package com.idp.idpapi.contract.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.idp.idpapi.contract.dto.request.ContractCreateRequest;
import com.idp.idpapi.contract.dto.request.ContractUpdateRequest;
import com.idp.idpapi.contract.dto.request.ContractUploadRequest;
import com.idp.idpapi.contract.dto.response.ContractDetailResponse;
import com.idp.idpapi.contract.dto.response.ContractFileResponse;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.contract.dto.response.ContractVersionResponse;
import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.entity.ContractFile;
import com.idp.idpapi.contract.entity.ContractVersion;
import com.idp.idpapi.contract.enums.ContractStatus;

@Component
public class ContractMapper {

    public void applyCreateRequest(Contract entity, ContractCreateRequest request) {
        applyContractValues(
                entity,
                request.contractNumber(),
                request.contractName(),
                request.partnerRepresentativeName(),
                request.partnerRepresentativePosition(),
                request.signedDate(),
                request.effectiveDate(),
                request.expiredDate(),
                request.totalValue(),
                request.currency(),
                request.status());
    }

    public void applyUpdateRequest(Contract entity, ContractUpdateRequest request) {
        applyContractValues(
                entity,
                request.contractNumber(),
                request.contractName(),
                request.partnerRepresentativeName(),
                request.partnerRepresentativePosition(),
                request.signedDate(),
                request.effectiveDate(),
                request.expiredDate(),
                request.totalValue(),
                request.currency(),
                request.status());
    }

    public void applyUploadRequest(Contract entity, ContractUploadRequest request) {
        applyContractValues(
                entity,
                request.getContractNumber(),
                request.getContractName(),
                request.getPartnerRepresentativeName(),
                request.getPartnerRepresentativePosition(),
                request.getSignedDate(),
                request.getEffectiveDate(),
                request.getExpiredDate(),
                request.getTotalValue(),
                request.getCurrency(),
                request.getStatus());
    }

    public ContractSummaryResponse toSummaryResponse(
            Contract contract,
            ContractVersion currentVersion,
            ContractFile latestFile,
            boolean favorite) {
        return new ContractSummaryResponse(
                contract.getContractId(),
                contract.getContractNumber(),
                contract.getContractName(),
                contract.getDocumentType().getDocumentTypeId(),
                contract.getDocumentType().getName(),
                contract.getPartner().getPartnerId(),
                contract.getPartner().getCompanyName(),
                contract.getStatus() != null ? contract.getStatus().getValue() : null,
                contract.getEffectiveDate(),
                contract.getExpiredDate(),
                contract.getTotalValue(),
                contract.getCurrency(),
                contract.getCurrentVersionId(),
                currentVersion != null ? currentVersion.getVersionNumber() : null,
                latestFile != null ? latestFile.getFileName() : null,
                favorite,
                contract.getCreatedAt(),
                contract.getUpdatedAt());
    }

    public ContractDetailResponse toDetailResponse(
            Contract contract,
            ContractVersion currentVersion,
            List<ContractVersion> versions,
            List<ContractFile> files,
            boolean favorite) {
        List<ContractVersionResponse> versionResponses = versions.stream()
                .map(this::toVersionResponse)
                .toList();
        List<ContractFileResponse> fileResponses = files.stream()
                .map(this::toFileResponse)
                .toList();

        return new ContractDetailResponse(
                contract.getContractId(),
                contract.getContractNumber(),
                contract.getContractName(),
                contract.getDocumentType().getDocumentTypeId(),
                contract.getDocumentType().getName(),
                contract.getPartner().getPartnerId(),
                contract.getPartner().getCompanyName(),
                contract.getUploadedBy().getUserId(),
                contract.getUploadedBy().getFullName(),
                contract.getPartnerRepresentativeName(),
                contract.getPartnerRepresentativePosition(),
                contract.getSignedDate(),
                contract.getEffectiveDate(),
                contract.getExpiredDate(),
                contract.getTotalValue(),
                contract.getCurrency(),
                contract.getStatus() != null ? contract.getStatus().getValue() : null,
                contract.getCurrentVersionId(),
                currentVersion != null ? currentVersion.getVersionNumber() : null,
                favorite,
                contract.getCreatedAt(),
                contract.getUpdatedAt(),
                versionResponses,
                fileResponses);
    }

    public ContractVersionResponse toVersionResponse(ContractVersion version) {
        return new ContractVersionResponse(
                version.getVersionId(),
                version.getVersionNumber(),
                version.getStatus() != null ? version.getStatus().getValue() : null,
                version.getChangeNote(),
                version.getIsCurrent(),
                version.getCreatedAt());
    }

    public ContractFileResponse toFileResponse(ContractFile file) {
        return new ContractFileResponse(
                file.getFileId(),
                file.getFileName(),
                file.getFileType(),
                file.getFileSize(),
                file.getPageCount(),
                file.getFileHash(),
                file.getUploadedAt());
    }

    private void applyContractValues(
            Contract entity,
            String contractNumber,
            String contractName,
            String partnerRepresentativeName,
            String partnerRepresentativePosition,
            java.time.LocalDate signedDate,
            java.time.LocalDate effectiveDate,
            java.time.LocalDate expiredDate,
            java.math.BigDecimal totalValue,
            String currency,
            ContractStatus status) {
        entity.setContractNumber(contractNumber.trim());
        entity.setContractName(contractName);
        entity.setPartnerRepresentativeName(partnerRepresentativeName);
        entity.setPartnerRepresentativePosition(partnerRepresentativePosition);
        entity.setSignedDate(signedDate);
        entity.setEffectiveDate(effectiveDate);
        entity.setExpiredDate(expiredDate);
        entity.setTotalValue(totalValue);
        entity.setCurrency(currency);
        entity.setStatus(status != null ? status : ContractStatus.DRAFT);
    }
}
