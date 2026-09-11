package com.idp.idpapi.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idp.idpapi.comment.entity.CommentMention;
import com.idp.idpapi.comment.entity.CommentMentionId;

public interface CommentMentionRepository extends JpaRepository<CommentMention, CommentMentionId> {

    @Query("""
            select cm.mentionedUser.userId
            from CommentMention cm
            where cm.comment.commentId = :commentId
            """)
    List<Integer> findMentionedUserIdsByCommentId(@Param("commentId") Integer commentId);

    void deleteByCommentCommentId(Integer commentId);
}
