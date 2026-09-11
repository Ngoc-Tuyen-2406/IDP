package com.idp.idpapi.ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.EmbeddingInfo;

public interface EmbeddingInfoRepository extends JpaRepository<EmbeddingInfo, Integer> {

    Optional<EmbeddingInfo> findByVersionVersionId(Integer versionId);
}
