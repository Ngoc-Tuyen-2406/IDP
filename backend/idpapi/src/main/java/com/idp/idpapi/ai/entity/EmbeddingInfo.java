package com.idp.idpapi.ai.entity;

import java.time.LocalDateTime;

import com.idp.idpapi.contract.entity.ContractVersion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "embedding_info")
public class EmbeddingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "embedding_id")
    private Integer embeddingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ContractVersion version;

    @Column(name = "vector_index", length = 100)
    private String vectorIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private AIModel model;

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getEmbeddingId() {
        return embeddingId;
    }

    public void setEmbeddingId(Integer embeddingId) {
        this.embeddingId = embeddingId;
    }

    public ContractVersion getVersion() {
        return version;
    }

    public void setVersion(ContractVersion version) {
        this.version = version;
    }

    public String getVectorIndex() {
        return vectorIndex;
    }

    public void setVectorIndex(String vectorIndex) {
        this.vectorIndex = vectorIndex;
    }

    public AIModel getModel() {
        return model;
    }

    public void setModel(AIModel model) {
        this.model = model;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
