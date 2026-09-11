package com.idp.idpapi.ai.dto.response;

public record EmbeddingChunkResponse(
        Integer chunkId,
        Integer chunkIndex,
        String chunkText,
        String vectorId,
        Integer pageNumber) {
}
