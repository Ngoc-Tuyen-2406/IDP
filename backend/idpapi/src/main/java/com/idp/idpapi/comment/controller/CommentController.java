package com.idp.idpapi.comment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.comment.dto.request.CommentCreateRequest;
import com.idp.idpapi.comment.dto.request.CommentUpdateRequest;
import com.idp.idpapi.comment.dto.response.CommentResponse;
import com.idp.idpapi.comment.service.CommentService;
import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.security.SecurityUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Comments")
@PreAuthorize("hasAuthority('COMMENT_MANAGE')")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/contracts/{contractId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getByContractId(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach comment thanh cong.",
                commentService.getByContractId(contractId)));
    }

    @PostMapping("/contracts/{contractId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable Integer contractId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao comment thanh cong.",
                commentService.create(contractId, request, extractUserId(currentUser))));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat comment thanh cong.",
                commentService.update(commentId, request, extractUserId(currentUser))));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        commentService.delete(commentId, extractUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Xoa comment thanh cong."));
    }

    @PostMapping("/comments/{commentId}/reply")
    public ResponseEntity<ApiResponse<CommentResponse>> reply(
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tra loi comment thanh cong.",
                commentService.reply(commentId, request, extractUserId(currentUser))));
    }

    private Integer extractUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phien dang nhap khong hop le.");
        }
        return currentUser.getUserId();
    }
}
