package com.idp.idpapi.chat.mapper;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.idp.idpapi.chat.dto.response.ChatMessageResponse;
import com.idp.idpapi.chat.entity.AIChatHistory;

@Component
public class ChatMapper {

    public ChatMessageResponse toResponse(AIChatHistory entity) {
        return new ChatMessageResponse(
                entity.getChatId(),
                entity.getVersion().getVersionId(),
                entity.getVersion().getContract().getContractId(),
                entity.getQuestion(),
                entity.getAnswer(),
                entity.getResponseTimeMs(),
                entity.getConversationId(),
                toList(entity.getSourceChunkIds()),
                entity.getCreatedAt());
    }

    private List<Integer> toList(Integer[] sourceChunkIds) {
        return sourceChunkIds == null ? List.of() : Arrays.asList(sourceChunkIds);
    }
}
