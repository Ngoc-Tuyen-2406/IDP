package com.idp.idpapi.notification.dto.request;

public record NotificationSettingsUpdateRequest(
        Boolean emailEnabled,
        Boolean systemNotification,
        Boolean contractNew,
        Boolean contractApproval,
        Boolean contractExpiring,
        Boolean commentMention) {
}
