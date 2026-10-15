package com.statikk.notification.service;

import com.statikk.notification.dto.NotificationDto;
import com.statikk.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationDto createAndSendNotification(Long userId, String title, String message, String type);
    List<NotificationDto> getNotificationsByUserId(Long userId);
    NotificationResponse markAsRead(Long notificationId);
    long getUnreadCount(Long userId);
}
