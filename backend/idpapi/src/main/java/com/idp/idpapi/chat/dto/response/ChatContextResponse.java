package com.idp.idpapi.chat.dto.response;

import java.util.List;

import com.idp.idpapi.ai.dto.response.MetadataFieldResponse;

public record ChatContextResponse(
        Integer contractId,
        Integer versionId,
        String summary,
        List<MetadataFieldResponse> metadata,
        List<String> chunks) {
}
