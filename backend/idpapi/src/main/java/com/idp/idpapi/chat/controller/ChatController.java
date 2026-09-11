package com.idp.idpapi.chat.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idp.idpapi.chat.dto.request.ChatAskRequest;
import com.idp.idpapi.chat.dto.request.ChatContextRequest;
import com.idp.idpapi.chat.dto.response.ChatContextResponse;
import com.idp.idpapi.chat.dto.response.ChatMessageResponse;
import com.idp.idpapi.chat.service.ChatService;
import com.idp.idpapi.common.api.ApiResponse;
import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.UnauthorizedException;
import com.idp.idpapi.security.SecurityUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "AI Chat")
@PreAuthorize("hasAuthority('AI_CHAT')")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> ask(
            @Valid @RequestBody ChatAskRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                "Hoi dap AI thanh cong.",
                chatService.ask(request, extractUserId(currentUser))));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<ChatMessageResponse>>> getHistory(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(required = false) UUID conversationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay lich su chat thanh cong.",
                chatService.getHistory(extractUserId(currentUser), conversationId, page, size)));
    }

    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> clearHistory(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        chatService.clearHistory(extractUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Xoa lich su chat thanh cong."));
    }

    @PostMapping("/context")
    public ResponseEntity<ApiResponse<ChatContextResponse>> context(
            @Valid @RequestBody ChatContextRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay context chat thanh cong.",
                chatService.buildContext(request)));
    }

    private Integer extractUserId(SecurityUserDetails currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Phien dang nhap khong hop le.");
        }
        return currentUser.getUserId();
    }
}
