package com.idp.idpapi.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Page<Notification> findByUserUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    void deleteByUserUserId(Integer userId);
}
