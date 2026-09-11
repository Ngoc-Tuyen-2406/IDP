package com.idp.idpapi.documenttype.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.documenttype.entity.DocumentType;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndDocumentTypeIdNot(String name, Integer documentTypeId);
}
