package com.idp.idpapi.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatMessageResponse(
        Integer chatId,
        Integer versionId,
        Integer contractId,
        String question,
        String answer,
        Integer responseTimeMs,
        UUID conversationId,
        List<Integer> sourceChunkIds,
        LocalDateTime createdAt) {
}
