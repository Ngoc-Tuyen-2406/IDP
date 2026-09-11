package com.idp.idpapi.chat.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.ai.dto.response.MetadataFieldResponse;
import com.idp.idpapi.ai.entity.AISummary;
import com.idp.idpapi.ai.entity.EmbeddingChunk;
import com.idp.idpapi.ai.entity.EmbeddingInfo;
import com.idp.idpapi.ai.entity.OCRResult;
import com.idp.idpapi.ai.mapper.AiMapper;
import com.idp.idpapi.ai.repository.AIMetadataRepository;
import com.idp.idpapi.ai.repository.AISummaryRepository;
import com.idp.idpapi.ai.repository.EmbeddingChunkRepository;
import com.idp.idpapi.ai.repository.EmbeddingInfoRepository;
import com.idp.idpapi.ai.repository.OCRResultRepository;
import com.idp.idpapi.ai.service.AIProcessingService;
import com.idp.idpapi.ai.service.AiGatewayService;
import com.idp.idpapi.chat.dto.request.ChatAskRequest;
import com.idp.idpapi.chat.dto.request.ChatContextRequest;
import com.idp.idpapi.chat.dto.response.ChatContextResponse;
import com.idp.idpapi.chat.dto.response.ChatMessageResponse;
import com.idp.idpapi.chat.entity.AIChatHistory;
import com.idp.idpapi.chat.mapper.ChatMapper;
import com.idp.idpapi.chat.repository.AIChatHistoryRepository;
import com.idp.idpapi.chat.service.ChatService;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.entity.ContractVersion;
import com.idp.idpapi.contract.repository.ContractRepository;
import com.idp.idpapi.contract.repository.ContractVersionRepository;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.repository.UserRepository;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final AIChatHistoryRepository aiChatHistoryRepository;
    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final UserRepository userRepository;
    private final OCRResultRepository ocrResultRepository;
    private final AIMetadataRepository aiMetadataRepository;
    private final AISummaryRepository aiSummaryRepository;
    private final EmbeddingInfoRepository embeddingInfoRepository;
    private final EmbeddingChunkRepository embeddingChunkRepository;
    private final AIProcessingService aiProcessingService;
    private final AiGatewayService aiGatewayService;
    private final AiMapper aiMapper;
    private final ChatMapper chatMapper;

    public ChatServiceImpl(
            AIChatHistoryRepository aiChatHistoryRepository,
            ContractRepository contractRepository,
            ContractVersionRepository contractVersionRepository,
            UserRepository userRepository,
            OCRResultRepository ocrResultRepository,
            AIMetadataRepository aiMetadataRepository,
            AISummaryRepository aiSummaryRepository,
            EmbeddingInfoRepository embeddingInfoRepository,
            EmbeddingChunkRepository embeddingChunkRepository,
            AIProcessingService aiProcessingService,
            AiGatewayService aiGatewayService,
            AiMapper aiMapper,
            ChatMapper chatMapper) {
        this.aiChatHistoryRepository = aiChatHistoryRepository;
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.userRepository = userRepository;
        this.ocrResultRepository = ocrResultRepository;
        this.aiMetadataRepository = aiMetadataRepository;
        this.aiSummaryRepository = aiSummaryRepository;
        this.embeddingInfoRepository = embeddingInfoRepository;
        this.embeddingChunkRepository = embeddingChunkRepository;
        this.aiProcessingService = aiProcessingService;
        this.aiGatewayService = aiGatewayService;
        this.aiMapper = aiMapper;
        this.chatMapper = chatMapper;
    }

    @Override
    public ChatMessageResponse ask(ChatAskRequest request, Integer currentUserId) {
        ContractVersion version = resolveVersion(request.contractId(), request.versionId());
        User currentUser = getUser(currentUserId);
        String text = ensureOcrText(version);
        String summary = ensureSummary(version);
        ensureEmbedding(version);

        AiGatewayService.ChatPayload payload = aiGatewayService.askQuestion(new AiGatewayService.ChatRequest(
                request.question().trim(),
                text,
                summary,
                request.conversationId()));

        AIChatHistory history = new AIChatHistory();
        history.setVersion(version);
        history.setUser(currentUser);
        history.setQuestion(request.question().trim());
        history.setAnswer(payload.answer());
        history.setResponseTimeMs(payload.responseTimeMs());
        history.setConversationId(payload.conversationId());
        history.setSourceChunkIds(payload.sourceChunkIds());
        history = aiChatHistoryRepository.save(history);
        return chatMapper.toResponse(history);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> getHistory(Integer currentUserId, UUID conversationId, int page, int size) {
        if (conversationId != null) {
            List<ChatMessageResponse> content = aiChatHistoryRepository
                    .findByUserUserIdAndConversationIdOrderByCreatedAtAsc(currentUserId, conversationId)
                    .stream()
                    .map(chatMapper::toResponse)
                    .toList();
            return PageResponse.from(new PageImpl<>(content, PageRequest.of(0, Math.max(content.size(), 1)), content.size()));
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(aiChatHistoryRepository.findByUserUserIdOrderByCreatedAtDesc(currentUserId, pageable)
                .map(chatMapper::toResponse));
    }

    @Override
    public void clearHistory(Integer currentUserId) {
        aiChatHistoryRepository.deleteByUserUserId(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatContextResponse buildContext(ChatContextRequest request) {
        ContractVersion version = resolveVersion(request.contractId(), request.versionId());
        Integer contractId = version.getContract().getContractId();
        List<MetadataFieldResponse> metadata = aiMetadataRepository.findByVersionVersionIdOrderByMetadataIdAsc(version.getVersionId())
                .stream()
                .map(aiMapper::toMetadataResponse)
                .toList();
        String summary = aiSummaryRepository.findByVersionVersionId(version.getVersionId())
                .map(AISummary::getSummary)
                .orElse(null);
        List<String> chunks = embeddingInfoRepository.findByVersionVersionId(version.getVersionId())
                .map(EmbeddingInfo::getEmbeddingId)
                .map(embeddingChunkRepository::findByEmbeddingEmbeddingIdOrderByChunkIndexAsc)
                .orElse(List.of())
                .stream()
                .map(EmbeddingChunk::getChunkText)
                .limit(5)
                .toList();
        return new ChatContextResponse(contractId, version.getVersionId(), summary, metadata, chunks);
    }

    private String ensureOcrText(ContractVersion version) {
        List<OCRResult> ocrResults = ocrResultRepository.findByVersionVersionIdOrderByPageNumberAscCreatedAtAsc(version.getVersionId());
        if (ocrResults.isEmpty()) {
            aiProcessingService.runOcr(new com.idp.idpapi.ai.dto.request.VersionProcessRequest(version.getVersionId()));
            ocrResults = ocrResultRepository.findByVersionVersionIdOrderByPageNumberAscCreatedAtAsc(version.getVersionId());
        }
        return ocrResults.stream()
                .map(OCRResult::getOcrText)
                .filter(text -> text != null && !text.isBlank())
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
    }

    private String ensureSummary(ContractVersion version) {
        return aiSummaryRepository.findByVersionVersionId(version.getVersionId())
                .map(AISummary::getSummary)
                .orElseGet(() -> aiProcessingService.generateSummary(version.getContract().getContractId()).summary());
    }

    private void ensureEmbedding(ContractVersion version) {
        if (embeddingInfoRepository.findByVersionVersionId(version.getVersionId()).isEmpty()) {
            aiProcessingService.createEmbeddings(version.getContract().getContractId());
        }
    }

    private ContractVersion resolveVersion(Integer contractId, Integer versionId) {
        if (versionId != null) {
            return contractVersionRepository.findById(versionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phien ban hop dong."));
        }
        if (contractId == null) {
            throw new ResourceNotFoundException("Can cung cap contractId hoac versionId.");
        }
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay hop dong."));
        if (contract.getCurrentVersionId() == null) {
            throw new ResourceNotFoundException("Hop dong chua co phien ban hien tai.");
        }
        return contractVersionRepository.findById(contract.getCurrentVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phien ban hop dong."));
    }

    private User getUser(Integer userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung."));
    }
}
