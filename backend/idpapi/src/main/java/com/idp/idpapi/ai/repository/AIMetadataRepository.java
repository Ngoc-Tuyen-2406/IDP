package com.idp.idpapi.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.AIMetadata;

public interface AIMetadataRepository extends JpaRepository<AIMetadata, Integer> {

    List<AIMetadata> findByVersionVersionIdOrderByMetadataIdAsc(Integer versionId);

    void deleteByVersionVersionId(Integer versionId);
}
