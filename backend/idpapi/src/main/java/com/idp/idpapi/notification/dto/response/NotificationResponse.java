package com.idp.idpapi.notification.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Integer notificationId,
        String title,
        String content,
        String type,
        String link,
        Boolean isRead,
        LocalDateTime createdAt) {
}
