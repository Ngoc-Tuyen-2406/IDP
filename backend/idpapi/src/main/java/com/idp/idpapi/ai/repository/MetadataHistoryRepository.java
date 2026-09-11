package com.idp.idpapi.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.MetadataHistory;

public interface MetadataHistoryRepository extends JpaRepository<MetadataHistory, Integer> {

    List<MetadataHistory> findByMetadataMetadataIdOrderByEditedAtDesc(Integer metadataId);
}
