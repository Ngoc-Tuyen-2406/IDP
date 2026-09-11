package com.idp.idpapi.notification.mapper;

import org.springframework.stereotype.Component;

import com.idp.idpapi.notification.dto.response.NotificationResponse;
import com.idp.idpapi.notification.dto.response.NotificationSettingsResponse;
import com.idp.idpapi.notification.entity.Notification;
import com.idp.idpapi.notification.entity.NotificationSetting;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getType(),
                notification.getLink(),
                notification.getIsRead(),
                notification.getCreatedAt());
    }

    public NotificationSettingsResponse toSettingsResponse(NotificationSetting setting) {
        return new NotificationSettingsResponse(
                setting.getUserId(),
                setting.getEmailEnabled(),
                setting.getSystemNotification(),
                setting.getContractNew(),
                setting.getContractApproval(),
                setting.getContractExpiring(),
                setting.getCommentMention(),
                setting.getUpdatedAt());
    }
}
