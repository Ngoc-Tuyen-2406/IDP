package com.idp.idpapi.contract.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.enums.ContractStatus;

public final class ContractSpecifications {

    private ContractSpecifications() {
    }

    public static Specification<Contract> keyword(String keyword) {
        return (root, query, builder) -> {
            if (keyword == null || keyword.isBlank()) {
                return builder.conjunction();
            }
            String search = "%" + keyword.trim().toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("contractNumber")), search),
                    builder.like(builder.lower(root.get("contractName")), search),
                    builder.like(builder.lower(root.join("partner").get("companyName")), search),
                    builder.like(builder.lower(root.join("partner").get("taxCode")), search));
        };
    }

    public static Specification<Contract> partnerId(Integer partnerId) {
        return (root, query, builder) -> partnerId == null
                ? builder.conjunction()
                : builder.equal(root.join("partner").get("partnerId"), partnerId);
    }

    public static Specification<Contract> documentTypeId(Integer documentTypeId) {
        return (root, query, builder) -> documentTypeId == null
                ? builder.conjunction()
                : builder.equal(root.join("documentType").get("documentTypeId"), documentTypeId);
    }

    public static Specification<Contract> status(ContractStatus status) {
        return (root, query, builder) -> status == null
                ? builder.conjunction()
                : builder.equal(root.get("status"), status);
    }

    public static Specification<Contract> effectiveDateFrom(LocalDate value) {
        return (root, query, builder) -> value == null
                ? builder.conjunction()
                : builder.greaterThanOrEqualTo(root.get("effectiveDate"), value);
    }

    public static Specification<Contract> effectiveDateTo(LocalDate value) {
        return (root, query, builder) -> value == null
                ? builder.conjunction()
                : builder.lessThanOrEqualTo(root.get("effectiveDate"), value);
    }

    public static Specification<Contract> expiredDateFrom(LocalDate value) {
        return (root, query, builder) -> value == null
                ? builder.conjunction()
                : builder.greaterThanOrEqualTo(root.get("expiredDate"), value);
    }

    public static Specification<Contract> expiredDateTo(LocalDate value) {
        return (root, query, builder) -> value == null
                ? builder.conjunction()
                : builder.lessThanOrEqualTo(root.get("expiredDate"), value);
    }
}
