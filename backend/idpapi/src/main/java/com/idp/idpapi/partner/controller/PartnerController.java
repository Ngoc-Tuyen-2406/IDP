package com.idp.idpapi.partner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.partner.dto.request.PartnerUpsertRequest;
import com.idp.idpapi.partner.dto.response.PartnerResponse;
import com.idp.idpapi.partner.service.PartnerService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/partners")
@Tag(name = "Partners")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PARTNER_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<PartnerResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đối tác thành công.",
                partnerService.getAll(keyword, page, size)));
    }

    @GetMapping("/{partnerId}")
    @PreAuthorize("hasAuthority('PARTNER_VIEW')")
    public ResponseEntity<ApiResponse<PartnerResponse>> getById(@PathVariable Integer partnerId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết đối tác thành công.",
                partnerService.getById(partnerId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    public ResponseEntity<ApiResponse<PartnerResponse>> create(@Valid @RequestBody PartnerUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo đối tác thành công.",
                partnerService.create(request)));
    }

    @PutMapping("/{partnerId}")
    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    public ResponseEntity<ApiResponse<PartnerResponse>> update(
            @PathVariable Integer partnerId,
            @Valid @RequestBody PartnerUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật đối tác thành công.",
                partnerService.update(partnerId, request)));
    }

    @DeleteMapping("/{partnerId}")
    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer partnerId) {
        partnerService.delete(partnerId);
        return ResponseEntity.ok(ApiResponse.success("Xóa đối tác thành công."));
    }
}
