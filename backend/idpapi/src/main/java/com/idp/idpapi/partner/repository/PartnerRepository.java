package com.idp.idpapi.partner.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.partner.entity.Partner;

public interface PartnerRepository extends JpaRepository<Partner, Integer> {

    boolean existsByTaxCodeIgnoreCase(String taxCode);

    boolean existsByTaxCodeIgnoreCaseAndPartnerIdNot(String taxCode, Integer partnerId);

    Page<Partner> findByCompanyNameContainingIgnoreCaseOrTaxCodeContainingIgnoreCase(
            String companyName,
            String taxCode,
            Pageable pageable);
}
