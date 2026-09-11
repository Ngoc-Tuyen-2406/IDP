package com.idp.idpapi.contract.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idp.idpapi.contract.entity.ContractVersion;

public interface ContractVersionRepository extends JpaRepository<ContractVersion, Integer> {

    @Query("""
            select coalesce(max(cv.versionNumber), 0)
            from ContractVersion cv
            where cv.contract.contractId = :contractId
            """)
    Integer findMaxVersionNumberByContractId(@Param("contractId") Integer contractId);

    List<ContractVersion> findByVersionIdIn(Collection<Integer> versionIds);

    List<ContractVersion> findByContractContractIdOrderByVersionNumberDesc(Integer contractId);

    @Modifying
    @Query("""
            update ContractVersion cv
            set cv.isCurrent = false
            where cv.contract.contractId = :contractId
              and cv.isCurrent = true
            """)
    int clearCurrentVersionFlag(@Param("contractId") Integer contractId);
}
