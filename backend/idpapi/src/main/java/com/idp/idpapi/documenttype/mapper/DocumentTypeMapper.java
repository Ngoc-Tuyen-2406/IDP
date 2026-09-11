package com.idp.idpapi.documenttype.mapper;

import org.springframework.stereotype.Component;

import com.idp.idpapi.documenttype.dto.request.DocumentTypeUpsertRequest;
import com.idp.idpapi.documenttype.dto.response.DocumentTypeResponse;
import com.idp.idpapi.documenttype.entity.DocumentType;

@Component
public class DocumentTypeMapper {

    public DocumentTypeResponse toResponse(DocumentType entity) {
        return new DocumentTypeResponse(
                entity.getDocumentTypeId(),
                entity.getName(),
                entity.getDescription());
    }

    public void updateEntity(DocumentType entity, DocumentTypeUpsertRequest request) {
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
    }
}
