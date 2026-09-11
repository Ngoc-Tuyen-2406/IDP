package com.idp.idpapi.contract.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.contract.entity.ContractFile;

public interface ContractFileRepository extends JpaRepository<ContractFile, Integer> {

    Optional<ContractFile> findTopByVersionVersionIdOrderByUploadedAtDesc(Integer versionId);

    List<ContractFile> findByVersionVersionIdIn(Collection<Integer> versionIds);

    List<ContractFile> findByVersionContractContractId(Integer contractId);
}
