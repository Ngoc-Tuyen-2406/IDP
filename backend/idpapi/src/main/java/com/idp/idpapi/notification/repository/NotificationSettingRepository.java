package com.idp.idpapi.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.notification.entity.NotificationSetting;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Integer> {
}
