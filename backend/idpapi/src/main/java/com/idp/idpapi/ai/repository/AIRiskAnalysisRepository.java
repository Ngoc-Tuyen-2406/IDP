package com.idp.idpapi.ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.AIRiskAnalysis;

public interface AIRiskAnalysisRepository extends JpaRepository<AIRiskAnalysis, Integer> {

    Optional<AIRiskAnalysis> findFirstByVersionVersionIdOrderByCreatedAtDesc(Integer versionId);
}
