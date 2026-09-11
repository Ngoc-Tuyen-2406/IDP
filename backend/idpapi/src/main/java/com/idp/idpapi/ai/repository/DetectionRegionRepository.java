package com.idp.idpapi.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.DetectionRegion;

public interface DetectionRegionRepository extends JpaRepository<DetectionRegion, Integer> {

    List<DetectionRegion> findByVersionVersionIdOrderByPageNumberAscRegionIdAsc(Integer versionId);

    void deleteByVersionVersionId(Integer versionId);
}
