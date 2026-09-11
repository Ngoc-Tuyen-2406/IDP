package com.idp.idpapi.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByContractContractIdOrderByCreatedAtAsc(Integer contractId);
}
