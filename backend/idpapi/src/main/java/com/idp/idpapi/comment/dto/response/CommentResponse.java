package com.idp.idpapi.comment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Integer commentId,
        Integer contractId,
        Integer metadataId,
        Integer parentCommentId,
        Integer userId,
        String userName,
        String content,
        Integer pageNumber,
        LocalDateTime createdAt,
        List<Integer> mentionedUserIds) {
}
