package com.idp.idpapi.documenttype.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.documenttype.dto.request.DocumentTypeUpsertRequest;
import com.idp.idpapi.documenttype.dto.response.DocumentTypeResponse;
import com.idp.idpapi.documenttype.entity.DocumentType;
import com.idp.idpapi.documenttype.mapper.DocumentTypeMapper;
import com.idp.idpapi.documenttype.repository.DocumentTypeRepository;
import com.idp.idpapi.documenttype.service.DocumentTypeService;

@Service
@Transactional
public class DocumentTypeServiceImpl implements DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTypeMapper documentTypeMapper;

    public DocumentTypeServiceImpl(
            DocumentTypeRepository documentTypeRepository,
            DocumentTypeMapper documentTypeMapper) {
        this.documentTypeRepository = documentTypeRepository;
        this.documentTypeMapper = documentTypeMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTypeResponse> getAll() {
        return documentTypeRepository.findAll()
                .stream()
                .map(documentTypeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTypeResponse getById(Integer documentTypeId) {
        return documentTypeMapper.toResponse(getEntity(documentTypeId));
    }

    @Override
    public DocumentTypeResponse create(DocumentTypeUpsertRequest request) {
        if (documentTypeRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BadRequestException("Loại tài liệu đã tồn tại.");
        }

        DocumentType entity = new DocumentType();
        documentTypeMapper.updateEntity(entity, request);
        return documentTypeMapper.toResponse(documentTypeRepository.save(entity));
    }

    @Override
    public DocumentTypeResponse update(Integer documentTypeId, DocumentTypeUpsertRequest request) {
        DocumentType entity = getEntity(documentTypeId);
        if (documentTypeRepository.existsByNameIgnoreCaseAndDocumentTypeIdNot(request.name().trim(), documentTypeId)) {
            throw new BadRequestException("Loại tài liệu đã tồn tại.");
        }
        documentTypeMapper.updateEntity(entity, request);
        return documentTypeMapper.toResponse(documentTypeRepository.save(entity));
    }

    @Override
    public void delete(Integer documentTypeId) {
        documentTypeRepository.delete(getEntity(documentTypeId));
    }

    private DocumentType getEntity(Integer documentTypeId) {
        return documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại tài liệu."));
    }
}
