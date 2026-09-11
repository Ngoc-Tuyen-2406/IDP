package com.idp.idpapi.chat.service;

import java.util.UUID;

import com.idp.idpapi.chat.dto.request.ChatAskRequest;
import com.idp.idpapi.chat.dto.request.ChatContextRequest;
import com.idp.idpapi.chat.dto.response.ChatContextResponse;
import com.idp.idpapi.chat.dto.response.ChatMessageResponse;
import com.idp.idpapi.common.api.PageResponse;

public interface ChatService {

    ChatMessageResponse ask(ChatAskRequest request, Integer currentUserId);

    PageResponse<ChatMessageResponse> getHistory(Integer currentUserId, UUID conversationId, int page, int size);

    void clearHistory(Integer currentUserId);

    ChatContextResponse buildContext(ChatContextRequest request);
}
