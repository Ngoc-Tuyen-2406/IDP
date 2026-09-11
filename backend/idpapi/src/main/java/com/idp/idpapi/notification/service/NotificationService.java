package com.idp.idpapi.notification.service;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.notification.dto.request.NotificationSettingsUpdateRequest;
import com.idp.idpapi.notification.dto.response.NotificationResponse;
import com.idp.idpapi.notification.dto.response.NotificationSettingsResponse;

public interface NotificationService {

    PageResponse<NotificationResponse> getNotifications(Integer userId, int page, int size);

    void markRead(Integer notificationId, Integer userId);

    void markAllRead(Integer userId);

    void delete(Integer notificationId, Integer userId);

    void deleteAll(Integer userId);

    NotificationSettingsResponse getSettings(Integer userId);

    NotificationSettingsResponse updateSettings(Integer userId, NotificationSettingsUpdateRequest request);

    void createNotification(Integer userId, String title, String content, String type, String link);
}
