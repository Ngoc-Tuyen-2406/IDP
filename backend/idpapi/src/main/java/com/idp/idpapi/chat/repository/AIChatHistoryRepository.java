package com.idp.idpapi.chat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.chat.entity.AIChatHistory;

public interface AIChatHistoryRepository extends JpaRepository<AIChatHistory, Integer> {

    Page<AIChatHistory> findByUserUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    List<AIChatHistory> findByUserUserIdAndConversationIdOrderByCreatedAtAsc(Integer userId, UUID conversationId);

    void deleteByUserUserId(Integer userId);
}
