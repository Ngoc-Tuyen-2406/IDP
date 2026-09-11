package com.idp.idpapi.ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.AISummary;

public interface AISummaryRepository extends JpaRepository<AISummary, Integer> {

    Optional<AISummary> findByVersionVersionId(Integer versionId);
}
