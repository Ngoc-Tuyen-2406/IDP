package com.idp.idpapi.contract.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.enums.ContractStatus;

public interface ContractRepository extends JpaRepository<Contract, Integer>, JpaSpecificationExecutor<Contract> {

    boolean existsByContractNumberIgnoreCase(String contractNumber);

    boolean existsByContractNumberIgnoreCaseAndContractIdNot(String contractNumber, Integer contractId);

    long countByStatus(ContractStatus status);

    long countByExpiredDateBetween(LocalDate startDate, LocalDate endDate);

    Page<Contract> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select c.status, count(c) from Contract c group by c.status")
    List<Object[]> countByStatusGrouped();
}
