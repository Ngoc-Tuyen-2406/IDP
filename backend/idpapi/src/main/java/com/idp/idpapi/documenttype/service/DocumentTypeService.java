package com.idp.idpapi.documenttype.service;

import java.util.List;

import com.idp.idpapi.documenttype.dto.request.DocumentTypeUpsertRequest;
import com.idp.idpapi.documenttype.dto.response.DocumentTypeResponse;

public interface DocumentTypeService {

    List<DocumentTypeResponse> getAll();

    DocumentTypeResponse getById(Integer documentTypeId);

    DocumentTypeResponse create(DocumentTypeUpsertRequest request);

    DocumentTypeResponse update(Integer documentTypeId, DocumentTypeUpsertRequest request);

    void delete(Integer documentTypeId);
}
