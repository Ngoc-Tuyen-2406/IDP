package com.idp.idpapi.contract.controller;

import java.time.LocalDate;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.contract.dto.request.ContractCreateRequest;
import com.idp.idpapi.contract.dto.request.ContractUpdateRequest;
import com.idp.idpapi.contract.dto.request.ContractUploadRequest;
import com.idp.idpapi.contract.dto.response.ContractDetailResponse;
import com.idp.idpapi.contract.dto.response.ContractDownloadPayload;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.contract.enums.ContractStatus;
import com.idp.idpapi.contract.service.ContractService;
import com.idp.idpapi.security.SecurityUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/contracts")
@Tag(name = "Contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<ContractSummaryResponse>>> getContracts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer partnerId,
            @RequestParam(required = false) Integer documentTypeId,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiredFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiredTo,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách hợp đồng thành công.",
                contractService.getContracts(
                        keyword,
                        partnerId,
                        documentTypeId,
                        status,
                        effectiveFrom,
                        effectiveTo,
                        expiredFrom,
                        expiredTo,
                        currentUser != null ? currentUser.getUserId() : null,
                        pageable)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<ContractSummaryResponse>>> searchContracts(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tìm kiếm hợp đồng thành công.",
                contractService.getContracts(keyword, null, null, null, null, null, null, null,
                        currentUser != null ? currentUser.getUserId() : null, pageable)));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<ContractSummaryResponse>>> filterContracts(
            @RequestParam(required = false) Integer partnerId,
            @RequestParam(required = false) Integer documentTypeId,
            @RequestParam(required = false) ContractStatus status,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lọc hợp đồng thành công.",
                contractService.getContracts(null, partnerId, documentTypeId, status, null, null, null, null,
                        currentUser != null ? currentUser.getUserId() : null, pageable)));
    }

    @GetMapping("/{contractId}")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> getById(
            @PathVariable Integer contractId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết hợp đồng thành công.",
                contractService.getById(contractId, resolveUserId(currentUser))));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTRACT_CREATE')")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> create(
            @Valid @RequestBody ContractCreateRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo hợp đồng thành công.",
                contractService.create(request, resolveUserId(currentUser))));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CONTRACT_CREATE')")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> upload(
            @Valid @ModelAttribute ContractUploadRequest request,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Upload hợp đồng thành công.",
                contractService.upload(request, file, resolveUserId(currentUser))));
    }

    @PutMapping("/{contractId}")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> update(
            @PathVariable Integer contractId,
            @Valid @RequestBody ContractUpdateRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật hợp đồng thành công.",
                contractService.update(contractId, request, resolveUserId(currentUser))));
    }

    @DeleteMapping("/{contractId}")
    @PreAuthorize("hasAuthority('CONTRACT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer contractId) {
        contractService.delete(contractId);
        return ResponseEntity.ok(ApiResponse.success("Xóa hợp đồng thành công."));
    }

    @GetMapping("/{contractId}/download")
    @PreAuthorize("hasAuthority('CONTRACT_DOWNLOAD')")
    public ResponseEntity<Resource> download(@PathVariable Integer contractId) {
        ContractDownloadPayload payload = contractService.downloadLatestFile(contractId);
        String contentType = payload.contentType() != null ? payload.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(payload.fileName())
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(contentType))
                .body(payload.resource());
    }

    @GetMapping("/{contractId}/file")
    @PreAuthorize("hasAuthority('CONTRACT_DOWNLOAD')")
    public ResponseEntity<Resource> getLatestFile(@PathVariable Integer contractId) {
        return download(contractId);
    }

    @GetMapping("/favorites")
    @PreAuthorize("hasAuthority('CONTRACT_FAVORITE')")
    public ResponseEntity<ApiResponse<PageResponse<ContractSummaryResponse>>> getFavorites(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách hợp đồng yêu thích thành công.",
                contractService.getFavoriteContracts(resolveUserId(currentUser), pageable)));
    }

    @PostMapping("/{contractId}/favorite")
    @PreAuthorize("hasAuthority('CONTRACT_FAVORITE')")
    public ResponseEntity<ApiResponse<Void>> markFavorite(
            @PathVariable Integer contractId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        contractService.markFavorite(contractId, resolveUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu yêu thích thành công."));
    }

    @DeleteMapping("/{contractId}/favorite")
    @PreAuthorize("hasAuthority('CONTRACT_FAVORITE')")
    public ResponseEntity<ApiResponse<Void>> unmarkFavorite(
            @PathVariable Integer contractId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        contractService.unmarkFavorite(contractId, resolveUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Bỏ đánh dấu yêu thích thành công."));
    }

    private Integer resolveUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phiên đăng nhập không hợp lệ.");
        }
        return currentUser.getUserId();
    }
}
