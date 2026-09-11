package com.idp.idpapi.processing.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.processing.entity.ProcessingQueue;

public interface ProcessingQueueRepository extends JpaRepository<ProcessingQueue, Integer> {

    Page<ProcessingQueue> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<ProcessingQueue> findFirstByVersionVersionIdAndStatusInOrderByCreatedAtDesc(
            Integer versionId,
            Collection<String> statuses);
}
