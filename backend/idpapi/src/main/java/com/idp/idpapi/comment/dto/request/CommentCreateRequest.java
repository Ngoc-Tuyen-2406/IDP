package com.idp.idpapi.comment.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        Integer metadataId,
        Integer parentCommentId,
        Integer pageNumber,

        @NotBlank(message = "content khong duoc de trong.")
        @Size(max = 5000, message = "content khong duoc vuot qua 5000 ky tu.")
        String content,

        Set<@NotNull(message = "mentionedUserId khong hop le.") Integer> mentionedUserIds) {
}
