package com.idp.idpapi.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.OCRResult;

public interface OCRResultRepository extends JpaRepository<OCRResult, Integer> {

    List<OCRResult> findByVersionVersionIdOrderByPageNumberAscCreatedAtAsc(Integer versionId);

    void deleteByVersionVersionId(Integer versionId);
}
