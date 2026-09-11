package com.idp.idpapi.notification.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.notification.dto.request.NotificationSettingsUpdateRequest;
import com.idp.idpapi.notification.dto.response.NotificationResponse;
import com.idp.idpapi.notification.dto.response.NotificationSettingsResponse;
import com.idp.idpapi.notification.entity.Notification;
import com.idp.idpapi.notification.entity.NotificationSetting;
import com.idp.idpapi.notification.mapper.NotificationMapper;
import com.idp.idpapi.notification.repository.NotificationRepository;
import com.idp.idpapi.notification.repository.NotificationSettingRepository;
import com.idp.idpapi.notification.service.NotificationService;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.repository.UserRepository;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationSettingRepository notificationSettingRepository,
            UserRepository userRepository,
            NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationSettingRepository = notificationSettingRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(Integer userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse));
    }

    @Override
    public void markRead(Integer notificationId, Integer userId) {
        Notification notification = getNotification(notificationId, userId);
        notification.setIsRead(Boolean.TRUE);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllRead(Integer userId) {
        notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Integer.MAX_VALUE))
                .forEach(notification -> notification.setIsRead(Boolean.TRUE));
    }

    @Override
    public void delete(Integer notificationId, Integer userId) {
        notificationRepository.delete(getNotification(notificationId, userId));
    }

    @Override
    public void deleteAll(Integer userId) {
        notificationRepository.deleteByUserUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSettingsResponse getSettings(Integer userId) {
        return notificationMapper.toSettingsResponse(getOrCreateSetting(userId));
    }

    @Override
    public NotificationSettingsResponse updateSettings(Integer userId, NotificationSettingsUpdateRequest request) {
        NotificationSetting setting = getOrCreateSetting(userId);
        if (request.emailEnabled() != null) {
            setting.setEmailEnabled(request.emailEnabled());
        }
        if (request.systemNotification() != null) {
            setting.setSystemNotification(request.systemNotification());
        }
        if (request.contractNew() != null) {
            setting.setContractNew(request.contractNew());
        }
        if (request.contractApproval() != null) {
            setting.setContractApproval(request.contractApproval());
        }
        if (request.contractExpiring() != null) {
            setting.setContractExpiring(request.contractExpiring());
        }
        if (request.commentMention() != null) {
            setting.setCommentMention(request.commentMention());
        }
        return notificationMapper.toSettingsResponse(notificationSettingRepository.save(setting));
    }

    @Override
    public void createNotification(Integer userId, String title, String content, String type, String link) {
        Notification notification = new Notification();
        notification.setUser(getUser(userId));
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setLink(link);
        notificationRepository.save(notification);
    }

    private Notification getNotification(Integer notificationId, Integer userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thong bao."));
        if (!notification.getUser().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Khong tim thay thong bao.");
        }
        return notification;
    }

    private NotificationSetting getOrCreateSetting(Integer userId) {
        return notificationSettingRepository.findById(userId)
                .orElseGet(() -> {
                    NotificationSetting setting = new NotificationSetting();
                    setting.setUser(getUser(userId));
                    setting.setUserId(userId);
                    return notificationSettingRepository.save(setting);
                });
    }

    private User getUser(Integer userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung."));
    }
}
