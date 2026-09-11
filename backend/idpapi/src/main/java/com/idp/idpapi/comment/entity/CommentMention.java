package com.idp.idpapi.comment.entity;

import com.idp.idpapi.user.entity.User;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "comment_mentions")
public class CommentMention {

    @EmbeddedId
    private CommentMentionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mentionedUserId")
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;

    public CommentMentionId getId() {
        return id;
    }

    public void setId(CommentMentionId id) {
        this.id = id;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public User getMentionedUser() {
        return mentionedUser;
    }

    public void setMentionedUser(User mentionedUser) {
        this.mentionedUser = mentionedUser;
    }
}
