package com.idp.idpapi.ai.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.idp.idpapi.ai.dto.response.DetectionRegionResponse;
import com.idp.idpapi.ai.dto.response.ContractClauseResponse;
import com.idp.idpapi.ai.dto.response.EmbeddingChunkResponse;
import com.idp.idpapi.ai.dto.response.EmbeddingInfoResponse;
import com.idp.idpapi.ai.dto.response.MetadataFieldResponse;
import com.idp.idpapi.ai.dto.response.OcrResultResponse;
import com.idp.idpapi.ai.dto.response.RiskAnalysisResponse;
import com.idp.idpapi.ai.dto.response.SummaryResponse;
import com.idp.idpapi.ai.entity.AIMetadata;
import com.idp.idpapi.ai.entity.AIClause;
import com.idp.idpapi.ai.entity.AIRiskAnalysis;
import com.idp.idpapi.ai.entity.AISummary;
import com.idp.idpapi.ai.entity.DetectionRegion;
import com.idp.idpapi.ai.entity.EmbeddingChunk;
import com.idp.idpapi.ai.entity.EmbeddingInfo;
import com.idp.idpapi.ai.entity.OCRResult;

@Component
public class AiMapper {

    public OcrResultResponse toOcrResponse(OCRResult entity) {
        return new OcrResultResponse(
                entity.getOcrId(),
                entity.getVersion().getVersionId(),
                entity.getPageNumber(),
                entity.getLanguage(),
                entity.getEngine(),
                entity.getOcrText(),
                entity.getConfidence(),
                entity.getCreatedAt());
    }

    public DetectionRegionResponse toDetectionResponse(DetectionRegion entity) {
        return new DetectionRegionResponse(
                entity.getRegionId(),
                entity.getVersion().getVersionId(),
                entity.getPageNumber(),
                entity.getLabel(),
                entity.getXMin(),
                entity.getYMin(),
                entity.getXMax(),
                entity.getYMax(),
                entity.getConfidence());
    }

    public ContractClauseResponse toClauseResponse(AIClause entity) {
        return new ContractClauseResponse(
                entity.getClauseId(),
                entity.getVersion().getVersionId(),
                entity.getClauseType(),
                entity.getTitle(),
                entity.getClauseText(),
                entity.getMatchedKeywords(),
                entity.getPageNumber(),
                entity.getConfidence(),
                entity.getModel() != null ? entity.getModel().getModelName() : null,
                entity.getCreatedAt());
    }

    public MetadataFieldResponse toMetadataResponse(AIMetadata entity) {
        return new MetadataFieldResponse(
                entity.getMetadataId(),
                entity.getVersion().getVersionId(),
                entity.getFieldName(),
                entity.getFieldType(),
                entity.getOriginalValue(),
                entity.getCurrentValue(),
                entity.getConfidence(),
                entity.getVerified(),
                entity.getVerifiedBy() != null ? entity.getVerifiedBy().getUserId() : null,
                entity.getVerifiedAt());
    }

    public SummaryResponse toSummaryResponse(AISummary entity) {
        return new SummaryResponse(
                entity.getSummaryId(),
                entity.getVersion().getVersionId(),
                entity.getSummary(),
                entity.getSummaryJson(),
                entity.getModel() != null ? entity.getModel().getModelName() : null,
                entity.getCreatedAt());
    }

    public RiskAnalysisResponse toRiskResponse(AIRiskAnalysis entity) {
        return new RiskAnalysisResponse(
                entity.getRiskId(),
                entity.getVersion().getVersionId(),
                entity.getRiskLevel(),
                entity.getRiskScore(),
                entity.getRiskSummary(),
                entity.getRecommendation(),
                entity.getRiskDetails(),
                entity.getModel() != null ? entity.getModel().getModelName() : null,
                entity.getCreatedAt());
    }

    public EmbeddingInfoResponse toEmbeddingResponse(EmbeddingInfo entity, List<EmbeddingChunk> chunks) {
        return new EmbeddingInfoResponse(
                entity.getEmbeddingId(),
                entity.getVersion().getVersionId(),
                entity.getVectorIndex(),
                entity.getChunkCount(),
                entity.getModel() != null ? entity.getModel().getModelName() : null,
                entity.getCreatedAt(),
                chunks.stream().map(this::toEmbeddingChunkResponse).toList());
    }

    public EmbeddingChunkResponse toEmbeddingChunkResponse(EmbeddingChunk entity) {
        return new EmbeddingChunkResponse(
                entity.getChunkId(),
                entity.getChunkIndex(),
                entity.getChunkText(),
                entity.getVectorId(),
                entity.getPageNumber());
    }
}
