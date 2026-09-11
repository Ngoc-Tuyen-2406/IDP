package com.idp.idpapi.comment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.idp.idpapi.comment.dto.response.CommentResponse;
import com.idp.idpapi.comment.entity.Comment;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment, List<Integer> mentionedUserIds) {
        return new CommentResponse(
                comment.getCommentId(),
                comment.getContract().getContractId(),
                comment.getMetadataId(),
                comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null,
                comment.getUser().getUserId(),
                comment.getUser().getFullName(),
                comment.getContent(),
                comment.getPageNumber(),
                comment.getCreatedAt(),
                mentionedUserIds);
    }
}
