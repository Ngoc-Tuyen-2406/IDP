package com.idp.idpapi.ai.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.idpapi.ai.dto.response.ContractClauseResponse;
import com.idp.idpapi.ai.dto.request.MetadataFieldUpdateRequest;
import com.idp.idpapi.ai.dto.request.MetadataUpdateRequest;
import com.idp.idpapi.ai.dto.request.VersionProcessRequest;
import com.idp.idpapi.ai.dto.response.DetectionRegionResponse;
import com.idp.idpapi.ai.dto.response.EmbeddingInfoResponse;
import com.idp.idpapi.ai.dto.response.MetadataFieldResponse;
import com.idp.idpapi.ai.dto.response.OcrResultResponse;
import com.idp.idpapi.ai.dto.response.RiskAnalysisResponse;
import com.idp.idpapi.ai.dto.response.SummaryResponse;
import com.idp.idpapi.ai.entity.AIMetadata;
import com.idp.idpapi.ai.entity.AIClause;
import com.idp.idpapi.ai.entity.AIModel;
import com.idp.idpapi.ai.entity.AIRiskAnalysis;
import com.idp.idpapi.ai.entity.AISummary;
import com.idp.idpapi.ai.entity.DetectionRegion;
import com.idp.idpapi.ai.entity.EmbeddingChunk;
import com.idp.idpapi.ai.entity.EmbeddingInfo;
import com.idp.idpapi.ai.entity.MetadataHistory;
import com.idp.idpapi.ai.entity.OCRResult;
import com.idp.idpapi.ai.mapper.AiMapper;
import com.idp.idpapi.ai.repository.AIMetadataRepository;
import com.idp.idpapi.ai.repository.AIClauseRepository;
import com.idp.idpapi.ai.repository.AIModelRepository;
import com.idp.idpapi.ai.repository.AIRiskAnalysisRepository;
import com.idp.idpapi.ai.repository.AISummaryRepository;
import com.idp.idpapi.ai.repository.DetectionRegionRepository;
import com.idp.idpapi.ai.repository.EmbeddingChunkRepository;
import com.idp.idpapi.ai.repository.EmbeddingInfoRepository;
import com.idp.idpapi.ai.repository.MetadataHistoryRepository;
import com.idp.idpapi.ai.repository.OCRResultRepository;
import com.idp.idpapi.ai.service.AIProcessingService;
import com.idp.idpapi.ai.service.AiGatewayService;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.entity.ContractFile;
import com.idp.idpapi.contract.entity.ContractVersion;
import com.idp.idpapi.contract.repository.ContractFileRepository;
import com.idp.idpapi.contract.repository.ContractRepository;
import com.idp.idpapi.contract.repository.ContractVersionRepository;
import com.idp.idpapi.processing.dto.request.CreateProcessingJobRequest;
import com.idp.idpapi.processing.dto.response.ProcessingJobResponse;
import com.idp.idpapi.processing.entity.ProcessingQueue;
import com.idp.idpapi.processing.event.ProcessingJobQueuedEvent;
import com.idp.idpapi.processing.mapper.ProcessingMapper;
import com.idp.idpapi.processing.repository.ProcessingQueueRepository;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.repository.UserRepository;

@Service
@Transactional
public class AIProcessingServiceImpl implements AIProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AIProcessingServiceImpl.class);
    private static final List<String> ACTIVE_JOB_STATUSES = List.of("QUEUED", "RUNNING", "Queued", "Running");
    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    private final OCRResultRepository ocrResultRepository;
    private final DetectionRegionRepository detectionRegionRepository;
    private final AIMetadataRepository aiMetadataRepository;
    private final AIClauseRepository aiClauseRepository;
    private final MetadataHistoryRepository metadataHistoryRepository;
    private final AISummaryRepository aiSummaryRepository;
    private final AIRiskAnalysisRepository aiRiskAnalysisRepository;
    private final EmbeddingInfoRepository embeddingInfoRepository;
    private final EmbeddingChunkRepository embeddingChunkRepository;
    private final AIModelRepository aiModelRepository;
    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final ContractFileRepository contractFileRepository;
    private final ProcessingQueueRepository processingQueueRepository;
    private final UserRepository userRepository;
    private final AiGatewayService aiGatewayService;
    private final AiMapper aiMapper;
    private final ProcessingMapper processingMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate requiresNewTransaction;

    public AIProcessingServiceImpl(
            OCRResultRepository ocrResultRepository,
            DetectionRegionRepository detectionRegionRepository,
            AIMetadataRepository aiMetadataRepository,
            AIClauseRepository aiClauseRepository,
            MetadataHistoryRepository metadataHistoryRepository,
            AISummaryRepository aiSummaryRepository,
            AIRiskAnalysisRepository aiRiskAnalysisRepository,
            EmbeddingInfoRepository embeddingInfoRepository,
            EmbeddingChunkRepository embeddingChunkRepository,
            AIModelRepository aiModelRepository,
            ContractRepository contractRepository,
            ContractVersionRepository contractVersionRepository,
            ContractFileRepository contractFileRepository,
            ProcessingQueueRepository processingQueueRepository,
            UserRepository userRepository,
            AiGatewayService aiGatewayService,
            AiMapper aiMapper,
            ProcessingMapper processingMapper,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher,
            PlatformTransactionManager transactionManager) {
        this.ocrResultRepository = ocrResultRepository;
        this.detectionRegionRepository = detectionRegionRepository;
        this.aiMetadataRepository = aiMetadataRepository;
        this.aiClauseRepository = aiClauseRepository;
        this.metadataHistoryRepository = metadataHistoryRepository;
        this.aiSummaryRepository = aiSummaryRepository;
        this.aiRiskAnalysisRepository = aiRiskAnalysisRepository;
        this.embeddingInfoRepository = embeddingInfoRepository;
        this.embeddingChunkRepository = embeddingChunkRepository;
        this.aiModelRepository = aiModelRepository;
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.contractFileRepository = contractFileRepository;
        this.processingQueueRepository = processingQueueRepository;
        this.userRepository = userRepository;
        this.aiGatewayService = aiGatewayService;
        this.aiMapper = aiMapper;
        this.processingMapper = processingMapper;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
    }

    @Override
    public List<OcrResultResponse> runOcr(VersionProcessRequest request) {
        ContractVersion version = getVersion(request.versionId());
        ContractFile file = getLatestFile(version.getVersionId());
        ocrResultRepository.deleteByVersionVersionId(version.getVersionId());
        AiGatewayService.OcrPayload payload = aiGatewayService.runOcr(toPipelineRequest(version, file));
        saveOcr(version, payload);
        return getOcr(version.getVersionId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrResultResponse> getOcr(Integer versionId) {
        getVersion(versionId);
        return ocrResultRepository.findByVersionVersionIdOrderByPageNumberAscCreatedAtAsc(versionId)
                .stream()
                .map(aiMapper::toOcrResponse)
                .toList();
    }

    @Override
    public List<DetectionRegionResponse> runDetection(VersionProcessRequest request) {
        ContractVersion version = getVersion(request.versionId());
        ContractFile file = getLatestFile(version.getVersionId());
        detectionRegionRepository.deleteByVersionVersionId(version.getVersionId());
        AiGatewayService.DetectionPayload payload = aiGatewayService.runDetection(toPipelineRequest(version, file));
        saveDetection(version, payload);
        return getDetection(version.getVersionId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetectionRegionResponse> getDetection(Integer versionId) {
        getVersion(versionId);
        return detectionRegionRepository.findByVersionVersionIdOrderByPageNumberAscRegionIdAsc(versionId)
                .stream()
                .map(aiMapper::toDetectionResponse)
                .toList();
    }

    @Override
    public List<ContractClauseResponse> extractClauses(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        ContractFile file = getLatestFile(version.getVersionId());
        aiClauseRepository.deleteByVersionVersionId(version.getVersionId());
        saveClauses(version, aiGatewayService.extractClauses(toPipelineRequest(version, file)));
        return getClauses(contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractClauseResponse> getClauses(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        return aiClauseRepository.findByVersionVersionIdOrderByPageNumberAscClauseIdAsc(version.getVersionId())
                .stream()
                .map(aiMapper::toClauseResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetadataFieldResponse> getMetadataByContract(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        return aiMetadataRepository.findByVersionVersionIdOrderByMetadataIdAsc(version.getVersionId())
                .stream()
                .map(aiMapper::toMetadataResponse)
                .toList();
    }

    @Override
    public List<MetadataFieldResponse> updateMetadata(Integer contractId, MetadataUpdateRequest request, Integer currentUserId) {
        ContractVersion version = getCurrentVersion(contractId);
        User currentUser = getUser(currentUserId);
        for (MetadataFieldUpdateRequest field : request.fields()) {
            AIMetadata metadata = field.metadataId() != null
                    ? aiMetadataRepository.findById(field.metadataId())
                        .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay metadata."))
                    : new AIMetadata();
            if (metadata.getMetadataId() == null) {
                metadata.setVersion(version);
                metadata.setFieldName(field.fieldName().trim());
                metadata.setFieldType(field.fieldType());
                metadata.setOriginalValue(field.currentValue());
            } else {
                MetadataHistory history = new MetadataHistory();
                history.setMetadata(metadata);
                history.setOldValue(metadata.getCurrentValue());
                history.setNewValue(field.currentValue());
                history.setEditedBy(currentUser);
                metadataHistoryRepository.save(history);
                metadata.setFieldName(field.fieldName().trim());
                metadata.setFieldType(field.fieldType());
            }
            metadata.setCurrentValue(field.currentValue());
            if (field.verified() != null) {
                metadata.setVerified(field.verified());
                metadata.setVerifiedBy(field.verified() ? currentUser : null);
                metadata.setVerifiedAt(field.verified() ? LocalDateTime.now() : null);
            }
            aiMetadataRepository.save(metadata);
        }
        return getMetadataByContract(contractId);
    }

    @Override
    public void deleteMetadata(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        aiMetadataRepository.deleteByVersionVersionId(version.getVersionId());
    }

    @Override
    public SummaryResponse generateSummary(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        ensureMetadata(version);
        String text = combinedOcrText(version.getVersionId());
        AiGatewayService.SummaryPayload payload = aiGatewayService.generateSummary(new AiGatewayService.SummaryRequest(
                text,
                toMetadataPayloads(version.getVersionId())));
        AISummary entity = aiSummaryRepository.findByVersionVersionId(version.getVersionId()).orElseGet(AISummary::new);
        entity.setVersion(version);
        entity.setSummary(payload.summary());
        entity.setSummaryJson(payload.summaryJson());
        entity.setModel(resolveModel(payload.model()));
        return aiMapper.toSummaryResponse(aiSummaryRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public SummaryResponse getSummary(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        return aiSummaryRepository.findByVersionVersionId(version.getVersionId())
                .map(aiMapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tom tat AI."));
    }

    @Override
    public RiskAnalysisResponse analyzeRisk(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        ensureMetadata(version);
        String text = combinedOcrText(version.getVersionId());
        AiGatewayService.RiskPayload payload = aiGatewayService.analyzeRisk(new AiGatewayService.RiskRequest(
                text,
                toMetadataPayloads(version.getVersionId())));
        AIRiskAnalysis entity = aiRiskAnalysisRepository.findFirstByVersionVersionIdOrderByCreatedAtDesc(version.getVersionId())
                .orElseGet(AIRiskAnalysis::new);
        entity.setVersion(version);
        entity.setRiskLevel(payload.riskLevel());
        entity.setRiskScore(payload.riskScore());
        entity.setRiskSummary(payload.riskSummary());
        entity.setRecommendation(payload.recommendation());
        entity.setRiskDetails(payload.riskDetails());
        entity.setModel(resolveModel(payload.model()));
        return aiMapper.toRiskResponse(aiRiskAnalysisRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAnalysisResponse getRisk(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        return aiRiskAnalysisRepository.findFirstByVersionVersionIdOrderByCreatedAtDesc(version.getVersionId())
                .map(aiMapper::toRiskResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bao cao rui ro."));
    }

    @Override
    public EmbeddingInfoResponse createEmbeddings(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        String text = combinedOcrText(version.getVersionId());
        AiGatewayService.EmbeddingPayload payload = aiGatewayService.createEmbedding(new AiGatewayService.EmbeddingRequest(text));

        EmbeddingInfo entity = embeddingInfoRepository.findByVersionVersionId(version.getVersionId()).orElseGet(EmbeddingInfo::new);
        entity.setVersion(version);
        entity.setVectorIndex(payload.vectorIndex());
        entity.setChunkCount(payload.chunkCount());
        entity.setModel(resolveModel(payload.model()));
        entity = embeddingInfoRepository.save(entity);

        embeddingChunkRepository.deleteByEmbeddingEmbeddingId(entity.getEmbeddingId());
        saveEmbeddingChunks(entity, payload);
        return getEmbeddings(contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public EmbeddingInfoResponse getEmbeddings(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        EmbeddingInfo entity = embeddingInfoRepository.findByVersionVersionId(version.getVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay embedding."));
        return aiMapper.toEmbeddingResponse(
                entity,
                embeddingChunkRepository.findByEmbeddingEmbeddingIdOrderByChunkIndexAsc(entity.getEmbeddingId()));
    }

    @Override
    public void deleteEmbeddings(Integer contractId) {
        ContractVersion version = getCurrentVersion(contractId);
        embeddingInfoRepository.findByVersionVersionId(version.getVersionId())
                .ifPresent(entity -> {
                    embeddingChunkRepository.deleteByEmbeddingEmbeddingId(entity.getEmbeddingId());
                    embeddingInfoRepository.delete(entity);
                });
    }

    @Override
    public ProcessingJobResponse createJob(CreateProcessingJobRequest request) {
        ContractVersion version = resolveVersion(request.contractId(), request.versionId());
        getLatestFile(version.getVersionId());

        ProcessingQueue activeJob = processingQueueRepository
                .findFirstByVersionVersionIdAndStatusInOrderByCreatedAtDesc(
                        version.getVersionId(),
                        ACTIVE_JOB_STATUSES)
                .orElse(null);
        if (activeJob != null) {
            return processingMapper.toResponse(activeJob);
        }

        ProcessingQueue job = new ProcessingQueue();
        job.setVersion(version);
        job.setTaskType(request.taskType() != null ? request.taskType() : "FULL_PIPELINE");
        job.setQueueName("ai-processing");
        job.setWorkerName("spring-backend");
        job.setStatus("QUEUED");
        job.setProgress((short) 0);
        job.setErrorMessage(null);
        job = processingQueueRepository.saveAndFlush(job);
        eventPublisher.publishEvent(new ProcessingJobQueuedEvent(job.getQueueId()));
        return processingMapper.toResponse(job);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void processQueuedJob(Integer jobId) {
        PipelineExecutionContext context = requiresNewTransaction.execute(status -> {
            ProcessingQueue job = getJobEntity(jobId);
            if (!"QUEUED".equalsIgnoreCase(job.getStatus())) {
                return null;
            }

            ContractVersion version = job.getVersion();
            ContractFile file = getLatestFile(version.getVersionId());
            job.setStatus("RUNNING");
            job.setStartedAt(LocalDateTime.now());
            job.setFinishedAt(null);
            job.setProgress((short) 10);
            job.setErrorMessage(null);
            processingQueueRepository.save(job);
            return new PipelineExecutionContext(
                    version.getVersionId(),
                    version.getContract().getContractId(),
                    file.getFilePath(),
                    file.getFileName());
        });

        if (context == null) {
            return;
        }

        try {
            AiGatewayService.FullPipelinePayload payload = aiGatewayService.processPipeline(
                    new AiGatewayService.PipelineRequest(
                            context.filePath(),
                            context.fileName(),
                            context.contractId(),
                            context.versionId()));

            requiresNewTransaction.executeWithoutResult(status -> saveCompletedJob(jobId, context.versionId(), payload));
        } catch (RuntimeException exception) {
            String failureMessage = failureMessage(exception);
            requiresNewTransaction.executeWithoutResult(status -> markJobFailed(jobId, failureMessage));
            LOGGER.error("AI processing job {} failed: {}", jobId, failureMessage, exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProcessingJobResponse> getJobs(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(processingQueueRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(processingMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessingJobResponse getJob(Integer jobId) {
        return processingMapper.toResponse(getJobEntity(jobId));
    }

    @Override
    public ProcessingJobResponse retry(Integer jobId) {
        ProcessingQueue existingJob = getJobEntity(jobId);
        CreateProcessingJobRequest retryRequest = new CreateProcessingJobRequest(
                existingJob.getVersion().getContract().getContractId(),
                existingJob.getVersion().getVersionId(),
                existingJob.getTaskType());
        ProcessingJobResponse response = createJob(retryRequest);
        existingJob.setRetryCount(existingJob.getRetryCount() + 1);
        processingQueueRepository.save(existingJob);
        return response;
    }

    @Override
    public void deleteJob(Integer jobId) {
        processingQueueRepository.delete(getJobEntity(jobId));
    }

    private void saveCompletedJob(
            Integer jobId,
            Integer versionId,
            AiGatewayService.FullPipelinePayload payload) {
        ProcessingQueue job = getJobEntity(jobId);
        ContractVersion version = getVersion(versionId);

        ocrResultRepository.deleteByVersionVersionId(versionId);
        detectionRegionRepository.deleteByVersionVersionId(versionId);
        aiMetadataRepository.deleteByVersionVersionId(versionId);
        aiClauseRepository.deleteByVersionVersionId(versionId);

        saveOcr(version, payload.ocr());
        saveDetection(version, payload.detection());
        saveMetadata(version, payload.metadata());
        saveClauses(version, payload.clauses());
        saveSummary(version, payload.summary());
        saveRisk(version, payload.risk());
        saveEmbedding(version, payload.embedding());

        job.setProgress((short) 100);
        job.setStatus("COMPLETED");
        job.setErrorMessage(null);
        job.setFinishedAt(LocalDateTime.now());
        processingQueueRepository.save(job);
    }

    private void markJobFailed(Integer jobId, String failureMessage) {
        ProcessingQueue job = getJobEntity(jobId);
        job.setStatus("FAILED");
        job.setErrorMessage(failureMessage);
        job.setFinishedAt(LocalDateTime.now());
        processingQueueRepository.save(job);
    }

    private String failureMessage(RuntimeException exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        String message = rootCause.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getMessage();
        }
        if (message == null || message.isBlank()) {
            message = "AI Server khong the xu ly tai lieu.";
        }
        message = message.trim();
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private record PipelineExecutionContext(
            Integer versionId,
            Integer contractId,
            String filePath,
            String fileName) {
    }

    private void ensureMetadata(ContractVersion version) {
        if (ocrResultRepository.findByVersionVersionIdOrderByPageNumberAscCreatedAtAsc(version.getVersionId()).isEmpty()) {
            runOcr(new VersionProcessRequest(version.getVersionId()));
        }
        if (aiMetadataRepository.findByVersionVersionIdOrderByMetadataIdAsc(version.getVersionId()).isEmpty()) {
            ContractFile file = getLatestFile(version.getVersionId());
            saveMetadata(version, aiGatewayService.extractMetadata(toPipelineRequest(version, file)));
        }
    }

    private String combinedOcrText(Integer versionId) {
        List<OCRResult> results = ocrResultRepository.findByVersionVersionIdOrderByPageNumberAscCreatedAtAsc(versionId);
        if (results.isEmpty()) {
            runOcr(new VersionProcessRequest(versionId));
            results = ocrResultRepository.findByVersionVersionIdOrderByPageNumberAscCreatedAtAsc(versionId);
        }
        return results.stream()
                .map(OCRResult::getOcrText)
                .filter(text -> text != null && !text.isBlank())
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
    }

    private List<AiGatewayService.MetadataFieldPayload> toMetadataPayloads(Integer versionId) {
        return aiMetadataRepository.findByVersionVersionIdOrderByMetadataIdAsc(versionId)
                .stream()
                .map(entity -> new AiGatewayService.MetadataFieldPayload(
                        entity.getFieldName(),
                        entity.getFieldType(),
                        entity.getOriginalValue(),
                        entity.getCurrentValue(),
                        entity.getConfidence(),
                        entity.getVerified()))
                .toList();
    }

    private void saveOcr(ContractVersion version, AiGatewayService.OcrPayload payload) {
        if (payload == null || payload.pages() == null) {
            return;
        }
        for (AiGatewayService.OcrPagePayload page : payload.pages()) {
            OCRResult result = new OCRResult();
            result.setVersion(version);
            result.setPageNumber(page.pageNumber());
            result.setLanguage(page.language());
            result.setEngine(page.engine());
            result.setOcrText(page.text());
            result.setConfidence(page.confidence());
            ocrResultRepository.save(result);
        }
    }

    private void saveDetection(ContractVersion version, AiGatewayService.DetectionPayload payload) {
        if (payload == null || payload.regions() == null) {
            return;
        }
        for (AiGatewayService.DetectionRegionPayload regionPayload : payload.regions()) {
            DetectionRegion region = new DetectionRegion();
            region.setVersion(version);
            region.setPageNumber(regionPayload.pageNumber());
            region.setLabel(regionPayload.label());
            region.setXMin(regionPayload.xMin());
            region.setYMin(regionPayload.yMin());
            region.setXMax(regionPayload.xMax());
            region.setYMax(regionPayload.yMax());
            region.setConfidence(regionPayload.confidence());
            detectionRegionRepository.save(region);
        }
    }

    private void saveMetadata(ContractVersion version, AiGatewayService.MetadataPayload payload) {
        if (payload == null || payload.fields() == null) {
            return;
        }
        for (AiGatewayService.MetadataFieldPayload field : payload.fields()) {
            AIMetadata metadata = new AIMetadata();
            metadata.setVersion(version);
            metadata.setFieldName(field.fieldName());
            metadata.setFieldType(field.fieldType());
            metadata.setOriginalValue(field.originalValue());
            metadata.setCurrentValue(field.currentValue());
            metadata.setConfidence(field.confidence());
            metadata.setVerified(Boolean.TRUE.equals(field.verified()));
            aiMetadataRepository.save(metadata);
        }
    }

    private void saveClauses(ContractVersion version, AiGatewayService.ClauseResultPayload payload) {
        if (payload == null || payload.clauses() == null) {
            return;
        }
        List<AIClause> clauses = new ArrayList<>();
        for (AiGatewayService.ClausePayload clausePayload : payload.clauses()) {
            AIClause clause = new AIClause();
            clause.setVersion(version);
            clause.setClauseType(clausePayload.clauseType());
            clause.setTitle(clausePayload.title());
            clause.setClauseText(clausePayload.text());
            clause.setMatchedKeywords(objectMapper.valueToTree(
                    clausePayload.matchedKeywords() != null ? clausePayload.matchedKeywords() : List.of()));
            clause.setPageNumber(clausePayload.pageNumber());
            clause.setConfidence(clausePayload.confidence());
            clause.setModel(resolveModel(payload.model()));
            clauses.add(clause);
        }
        aiClauseRepository.saveAll(clauses);
    }

    private void saveSummary(ContractVersion version, AiGatewayService.SummaryPayload payload) {
        if (payload == null) {
            return;
        }
        AISummary summary = aiSummaryRepository.findByVersionVersionId(version.getVersionId()).orElseGet(AISummary::new);
        summary.setVersion(version);
        summary.setSummary(payload.summary());
        summary.setSummaryJson(payload.summaryJson());
        summary.setModel(resolveModel(payload.model()));
        aiSummaryRepository.save(summary);
    }

    private void saveRisk(ContractVersion version, AiGatewayService.RiskPayload payload) {
        if (payload == null) {
            return;
        }
        AIRiskAnalysis risk = aiRiskAnalysisRepository.findFirstByVersionVersionIdOrderByCreatedAtDesc(version.getVersionId())
                .orElseGet(AIRiskAnalysis::new);
        risk.setVersion(version);
        risk.setRiskLevel(payload.riskLevel());
        risk.setRiskScore(payload.riskScore());
        risk.setRiskSummary(payload.riskSummary());
        risk.setRecommendation(payload.recommendation());
        risk.setRiskDetails(payload.riskDetails());
        risk.setModel(resolveModel(payload.model()));
        aiRiskAnalysisRepository.save(risk);
    }

    private void saveEmbedding(ContractVersion version, AiGatewayService.EmbeddingPayload payload) {
        if (payload == null) {
            return;
        }
        EmbeddingInfo entity = embeddingInfoRepository.findByVersionVersionId(version.getVersionId()).orElseGet(EmbeddingInfo::new);
        entity.setVersion(version);
        entity.setVectorIndex(payload.vectorIndex());
        entity.setChunkCount(payload.chunkCount());
        entity.setModel(resolveModel(payload.model()));
        entity = embeddingInfoRepository.save(entity);
        embeddingChunkRepository.deleteByEmbeddingEmbeddingId(entity.getEmbeddingId());
        saveEmbeddingChunks(entity, payload);
    }

    private void saveEmbeddingChunks(EmbeddingInfo entity, AiGatewayService.EmbeddingPayload payload) {
        if (payload == null || payload.chunks() == null) {
            return;
        }
        List<EmbeddingChunk> chunks = new ArrayList<>();
        for (AiGatewayService.EmbeddingChunkPayload chunkPayload : payload.chunks()) {
            EmbeddingChunk chunk = new EmbeddingChunk();
            chunk.setEmbedding(entity);
            chunk.setChunkIndex(chunkPayload.chunkIndex());
            chunk.setChunkText(chunkPayload.chunkText());
            chunk.setVectorId(chunkPayload.vectorId());
            chunk.setPageNumber(chunkPayload.pageNumber());
            chunks.add(chunk);
        }
        embeddingChunkRepository.saveAll(chunks);
    }

    private AIModel resolveModel(AiGatewayService.ModelPayload payload) {
        if (payload == null || payload.modelName() == null || payload.modelName().isBlank()) {
            return null;
        }
        return aiModelRepository.findByModelNameIgnoreCase(payload.modelName().trim())
                .map(existing -> {
                    existing.setModelType(payload.modelType());
                    existing.setVersion(payload.version());
                    existing.setDescription(payload.description());
                    return aiModelRepository.save(existing);
                })
                .orElseGet(() -> {
                    AIModel model = new AIModel();
                    model.setModelName(payload.modelName().trim());
                    model.setModelType(payload.modelType());
                    model.setVersion(payload.version());
                    model.setDescription(payload.description());
                    return aiModelRepository.save(model);
                });
    }

    private ContractVersion resolveVersion(Integer contractId, Integer versionId) {
        if (versionId != null) {
            return getVersion(versionId);
        }
        if (contractId == null) {
            throw new ResourceNotFoundException("Can cung cap contractId hoac versionId.");
        }
        return getCurrentVersion(contractId);
    }

    private ContractVersion getCurrentVersion(Integer contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay hop dong."));
        if (contract.getCurrentVersionId() == null) {
            throw new ResourceNotFoundException("Hop dong chua co phien ban hien tai.");
        }
        return getVersion(contract.getCurrentVersionId());
    }

    private ContractVersion getVersion(Integer versionId) {
        return contractVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phien ban hop dong."));
    }

    private ContractFile getLatestFile(Integer versionId) {
        return contractFileRepository.findTopByVersionVersionIdOrderByUploadedAtDesc(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay file hop dong."));
    }

    private User getUser(Integer userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung."));
    }

    private ProcessingQueue getJobEntity(Integer jobId) {
        return processingQueueRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay job xu ly."));
    }

    private AiGatewayService.PipelineRequest toPipelineRequest(ContractVersion version, ContractFile file) {
        return new AiGatewayService.PipelineRequest(
                file.getFilePath(),
                file.getFileName(),
                version.getContract().getContractId(),
                version.getVersionId());
    }
}
