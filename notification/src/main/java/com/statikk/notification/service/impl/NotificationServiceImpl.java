package com.statikk.notification.service.impl;

import com.statikk.notification.dto.NotificationDto;
import com.statikk.notification.dto.NotificationResponse;
import com.statikk.notification.entity.NotificationRecord;
import com.statikk.notification.exception.ResourceNotFoundException;
import com.statikk.notification.repository.NotificationRepository;
import com.statikk.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public NotificationDto createAndSendNotification(Long userId, String title, String message, String type) {
        log.info("Creating notification for userId: {}, title: '{}'", userId, title);

        NotificationRecord record = NotificationRecord.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type != null ? type : "INFO")
                .readStatus(false)
                .build();

        NotificationRecord saved = notificationRepository.save(record);
        NotificationDto dto = mapToDto(saved);

        // Broadcast real-time message via WebSocket STOMP
        try {
            String destination = "/topic/notifications/" + userId;
            messagingTemplate.convertAndSend(destination, dto);
            log.info("Dispatched WebSocket STOMP message to: {}", destination);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message: {}", e.getMessage());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        NotificationRecord record = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        record.setReadStatus(true);
        notificationRepository.save(record);

        long unreadCount = notificationRepository.countByUserIdAndReadStatusFalse(record.getUserId());

        return NotificationResponse.builder()
                .id(notificationId)
                .message("Notification marked as read")
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadStatusFalse(userId);
    }

    private NotificationDto mapToDto(NotificationRecord record) {
        return NotificationDto.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .title(record.getTitle())
                .message(record.getMessage())
                .type(record.getType())
                .readStatus(record.isReadStatus())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
