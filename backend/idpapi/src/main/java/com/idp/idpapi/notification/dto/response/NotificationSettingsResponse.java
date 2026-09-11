package com.idp.idpapi.notification.dto.response;

import java.time.LocalDateTime;

public record NotificationSettingsResponse(
        Integer userId,
        Boolean emailEnabled,
        Boolean systemNotification,
        Boolean contractNew,
        Boolean contractApproval,
        Boolean contractExpiring,
        Boolean commentMention,
        LocalDateTime updatedAt) {
}
