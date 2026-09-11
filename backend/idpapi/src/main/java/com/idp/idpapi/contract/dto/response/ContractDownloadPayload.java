package com.idp.idpapi.contract.dto.response;

import org.springframework.core.io.Resource;

public record ContractDownloadPayload(
        Resource resource,
        String fileName,
        String contentType) {
}
