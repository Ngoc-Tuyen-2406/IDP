package com.idp.idpapi.contract.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.idp.idpapi.contract.enums.ContractStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ContractUploadRequest {

    @NotNull(message = "Loại tài liệu là bắt buộc.")
    private Integer documentTypeId;

    @NotNull(message = "Đối tác là bắt buộc.")
    private Integer partnerId;

    @NotBlank(message = "Số hợp đồng là bắt buộc.")
    @Size(max = 100, message = "Số hợp đồng không được vượt quá 100 ký tự.")
    private String contractNumber;

    @Size(max = 255, message = "Tên hợp đồng không được vượt quá 255 ký tự.")
    private String contractName;

    @Size(max = 255, message = "Tên người đại diện không được vượt quá 255 ký tự.")
    private String partnerRepresentativeName;

    @Size(max = 255, message = "Chức vụ người đại diện không được vượt quá 255 ký tự.")
    private String partnerRepresentativePosition;

    private LocalDate signedDate;
    private LocalDate effectiveDate;
    private LocalDate expiredDate;
    private BigDecimal totalValue;

    @Size(max = 10, message = "Mã tiền tệ không được vượt quá 10 ký tự.")
    private String currency;

    private ContractStatus status;
    private String changeNote;

    public Integer getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Integer documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getPartnerRepresentativeName() {
        return partnerRepresentativeName;
    }

    public void setPartnerRepresentativeName(String partnerRepresentativeName) {
        this.partnerRepresentativeName = partnerRepresentativeName;
    }

    public String getPartnerRepresentativePosition() {
        return partnerRepresentativePosition;
    }

    public void setPartnerRepresentativePosition(String partnerRepresentativePosition) {
        this.partnerRepresentativePosition = partnerRepresentativePosition;
    }

    public LocalDate getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDate signedDate) {
        this.signedDate = signedDate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public String getChangeNote() {
        return changeNote;
    }

    public void setChangeNote(String changeNote) {
        this.changeNote = changeNote;
    }
}
