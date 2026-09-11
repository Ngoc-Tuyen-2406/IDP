package com.idp.idpapi.ai.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record EmbeddingInfoResponse(
        Integer embeddingId,
        Integer versionId,
        String vectorIndex,
        Integer chunkCount,
        String modelName,
        LocalDateTime createdAt,
        List<EmbeddingChunkResponse> chunks) {
}
