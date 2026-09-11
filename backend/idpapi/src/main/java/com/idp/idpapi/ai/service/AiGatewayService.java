package com.idp.idpapi.ai.service;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public interface AiGatewayService {

    record PipelineRequest(
            String filePath,
            String fileName,
            Integer contractId,
            Integer versionId) {
    }

    record ModelPayload(
            String modelName,
            String modelType,
            String version,
            String description) {
    }

    record OcrPagePayload(
            Integer pageNumber,
            String language,
            String engine,
            String text,
            Double confidence) {
    }

    record OcrPayload(
            List<OcrPagePayload> pages,
            ModelPayload model) {
    }

    record DetectionRegionPayload(
            Integer pageNumber,
            String label,
            Double xMin,
            Double yMin,
            Double xMax,
            Double yMax,
            Double confidence) {
    }

    record DetectionPayload(
            List<DetectionRegionPayload> regions,
            ModelPayload model) {
    }

    record MetadataFieldPayload(
            String fieldName,
            String fieldType,
            JsonNode originalValue,
            JsonNode currentValue,
            Double confidence,
            Boolean verified) {
    }

    record MetadataPayload(
            List<MetadataFieldPayload> fields,
            ModelPayload model) {
    }

    record ClausePayload(
            String clauseType,
            String title,
            String text,
            List<String> matchedKeywords,
            Integer pageNumber,
            Double confidence) {
    }

    record ClauseResultPayload(
            List<ClausePayload> clauses,
            ModelPayload model) {
    }

    record SummaryPayload(
            String summary,
            JsonNode summaryJson,
            ModelPayload model) {
    }

    record RiskPayload(
            String riskLevel,
            Double riskScore,
            String riskSummary,
            String recommendation,
            JsonNode riskDetails,
            ModelPayload model) {
    }

    record EmbeddingChunkPayload(
            Integer chunkIndex,
            String chunkText,
            String vectorId,
            Integer pageNumber) {
    }

    record EmbeddingPayload(
            String vectorIndex,
            Integer chunkCount,
            List<EmbeddingChunkPayload> chunks,
            ModelPayload model) {
    }

    record FullPipelinePayload(
            OcrPayload ocr,
            DetectionPayload detection,
            MetadataPayload metadata,
            ClauseResultPayload clauses,
            SummaryPayload summary,
            RiskPayload risk,
            EmbeddingPayload embedding) {
    }

    record ChatPayload(
            String answer,
            Integer responseTimeMs,
            UUID conversationId,
            Integer[] sourceChunkIds) {
    }

    record SummaryRequest(
            String text,
            List<MetadataFieldPayload> metadata) {
    }

    record RiskRequest(
            String text,
            List<MetadataFieldPayload> metadata) {
    }

    record EmbeddingRequest(
            String text) {
    }

    record ChatRequest(
            String question,
            String text,
            String summary,
            UUID conversationId) {
    }

    FullPipelinePayload processPipeline(PipelineRequest request);

    OcrPayload runOcr(PipelineRequest request);

    DetectionPayload runDetection(PipelineRequest request);

    MetadataPayload extractMetadata(PipelineRequest request);

    ClauseResultPayload extractClauses(PipelineRequest request);

    SummaryPayload generateSummary(SummaryRequest request);

    RiskPayload analyzeRisk(RiskRequest request);

    EmbeddingPayload createEmbedding(EmbeddingRequest request);

    ChatPayload askQuestion(ChatRequest request);
}
