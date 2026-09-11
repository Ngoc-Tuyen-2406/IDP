package com.idp.idpapi.ai.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;
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
@Table(name = "ai_clauses")
public class AIClause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clause_id")
    private Integer clauseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ContractVersion version;

    @Column(name = "clause_type", nullable = false, length = 100)
    private String clauseType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "clause_text", columnDefinition = "TEXT", nullable = false)
    private String clauseText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_keywords", columnDefinition = "jsonb")
    private JsonNode matchedKeywords;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "confidence")
    private Double confidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private AIModel model;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getClauseId() {
        return clauseId;
    }

    public ContractVersion getVersion() {
        return version;
    }

    public void setVersion(ContractVersion version) {
        this.version = version;
    }

    public String getClauseType() {
        return clauseType;
    }

    public void setClauseType(String clauseType) {
        this.clauseType = clauseType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClauseText() {
        return clauseText;
    }

    public void setClauseText(String clauseText) {
        this.clauseText = clauseText;
    }

    public JsonNode getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(JsonNode matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public AIModel getModel() {
        return model;
    }

    public void setModel(AIModel model) {
        this.model = model;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
