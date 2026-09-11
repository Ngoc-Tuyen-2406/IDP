package com.idp.idpapi.chat.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record ChatAskRequest(
        Integer contractId,
        Integer versionId,

        @NotBlank(message = "question khong duoc de trong.")
        String question,

        UUID conversationId) {
}
