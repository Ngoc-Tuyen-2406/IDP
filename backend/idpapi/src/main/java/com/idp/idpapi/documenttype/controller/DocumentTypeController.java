package com.idp.idpapi.documenttype.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.documenttype.dto.request.DocumentTypeUpsertRequest;
import com.idp.idpapi.documenttype.dto.response.DocumentTypeResponse;
import com.idp.idpapi.documenttype.service.DocumentTypeService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/document-types")
@Tag(name = "Document Types")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    public DocumentTypeController(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_TYPE_VIEW')")
    public ResponseEntity<ApiResponse<List<DocumentTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách loại tài liệu thành công.",
                documentTypeService.getAll()));
    }

    @GetMapping("/{documentTypeId}")
    @PreAuthorize("hasAuthority('DOCUMENT_TYPE_VIEW')")
    public ResponseEntity<ApiResponse<DocumentTypeResponse>> getById(@PathVariable Integer documentTypeId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết loại tài liệu thành công.",
                documentTypeService.getById(documentTypeId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT_TYPE_MANAGE')")
    public ResponseEntity<ApiResponse<DocumentTypeResponse>> create(
            @Valid @RequestBody DocumentTypeUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo loại tài liệu thành công.",
                documentTypeService.create(request)));
    }

    @PutMapping("/{documentTypeId}")
    @PreAuthorize("hasAuthority('DOCUMENT_TYPE_MANAGE')")
    public ResponseEntity<ApiResponse<DocumentTypeResponse>> update(
            @PathVariable Integer documentTypeId,
            @Valid @RequestBody DocumentTypeUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật loại tài liệu thành công.",
                documentTypeService.update(documentTypeId, request)));
    }

    @DeleteMapping("/{documentTypeId}")
    @PreAuthorize("hasAuthority('DOCUMENT_TYPE_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer documentTypeId) {
        documentTypeService.delete(documentTypeId);
        return ResponseEntity.ok(ApiResponse.success("Xóa loại tài liệu thành công."));
    }
}
