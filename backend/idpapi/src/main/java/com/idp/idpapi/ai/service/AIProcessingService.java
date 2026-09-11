package com.idp.idpapi.ai.service;

import java.util.List;

import com.idp.idpapi.ai.dto.request.MetadataUpdateRequest;
import com.idp.idpapi.ai.dto.request.VersionProcessRequest;
import com.idp.idpapi.ai.dto.response.DetectionRegionResponse;
import com.idp.idpapi.ai.dto.response.ContractClauseResponse;
import com.idp.idpapi.ai.dto.response.EmbeddingInfoResponse;
import com.idp.idpapi.ai.dto.response.MetadataFieldResponse;
import com.idp.idpapi.ai.dto.response.OcrResultResponse;
import com.idp.idpapi.ai.dto.response.RiskAnalysisResponse;
import com.idp.idpapi.ai.dto.response.SummaryResponse;
import com.idp.idpapi.processing.dto.request.CreateProcessingJobRequest;
import com.idp.idpapi.processing.dto.response.ProcessingJobResponse;
import com.idp.idpapi.common.api.PageResponse;

public interface AIProcessingService {

    List<OcrResultResponse> runOcr(VersionProcessRequest request);

    List<OcrResultResponse> getOcr(Integer versionId);

    List<DetectionRegionResponse> runDetection(VersionProcessRequest request);

    List<DetectionRegionResponse> getDetection(Integer versionId);

    List<ContractClauseResponse> extractClauses(Integer contractId);

    List<ContractClauseResponse> getClauses(Integer contractId);

    List<MetadataFieldResponse> getMetadataByContract(Integer contractId);

    List<MetadataFieldResponse> updateMetadata(Integer contractId, MetadataUpdateRequest request, Integer currentUserId);

    void deleteMetadata(Integer contractId);

    SummaryResponse generateSummary(Integer contractId);

    SummaryResponse getSummary(Integer contractId);

    RiskAnalysisResponse analyzeRisk(Integer contractId);

    RiskAnalysisResponse getRisk(Integer contractId);

    EmbeddingInfoResponse createEmbeddings(Integer contractId);

    EmbeddingInfoResponse getEmbeddings(Integer contractId);

    void deleteEmbeddings(Integer contractId);

    ProcessingJobResponse createJob(CreateProcessingJobRequest request);

    void processQueuedJob(Integer jobId);

    PageResponse<ProcessingJobResponse> getJobs(int page, int size);

    ProcessingJobResponse getJob(Integer jobId);

    ProcessingJobResponse retry(Integer jobId);

    void deleteJob(Integer jobId);
}
