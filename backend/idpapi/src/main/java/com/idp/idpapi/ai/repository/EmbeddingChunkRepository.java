package com.idp.idpapi.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.EmbeddingChunk;

public interface EmbeddingChunkRepository extends JpaRepository<EmbeddingChunk, Integer> {

    List<EmbeddingChunk> findByEmbeddingEmbeddingIdOrderByChunkIndexAsc(Integer embeddingId);

    void deleteByEmbeddingEmbeddingId(Integer embeddingId);
}
