package com.idp.idpapi.comment.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CommentMentionId implements Serializable {

    @Column(name = "comment_id")
    private Integer commentId;

    @Column(name = "mentioned_user_id")
    private Integer mentionedUserId;

    public CommentMentionId() {
    }

    public CommentMentionId(Integer commentId, Integer mentionedUserId) {
        this.commentId = commentId;
        this.mentionedUserId = mentionedUserId;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public Integer getMentionedUserId() {
        return mentionedUserId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CommentMentionId that)) {
            return false;
        }
        return Objects.equals(commentId, that.commentId)
                && Objects.equals(mentionedUserId, that.mentionedUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, mentionedUserId);
    }
}
