package com.statikk.notification.service;

import com.statikk.notification.dto.NotificationDto;
import com.statikk.notification.dto.NotificationResponse;
import com.statikk.notification.entity.NotificationRecord;
import com.statikk.notification.exception.ResourceNotFoundException;
import com.statikk.notification.repository.NotificationRepository;
import com.statikk.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = NotificationRecord.builder()
                .id(1L)
                .userId(10L)
                .title("Planilla procesada")
                .message("Se importaron 10 partidas")
                .type("FILE_PROCESSED")
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create notification, persist to DB, and broadcast via WebSocket STOMP")
    void shouldCreateAndSendNotification() {
        when(notificationRepository.save(any(NotificationRecord.class))).thenReturn(sampleRecord);

        NotificationDto result = notificationService.createAndSendNotification(
                10L, "Planilla procesada", "Se importaron 10 partidas", "FILE_PROCESSED"
        );

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Planilla procesada");
        assertThat(result.getUserId()).isEqualTo(10L);

        verify(notificationRepository, times(1)).save(any(NotificationRecord.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/10"), any(NotificationDto.class));
    }

    @Test
    @DisplayName("Should fetch notifications for user")
    void shouldGetNotificationsByUserId() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(sampleRecord));

        List<NotificationDto> results = notificationService.getNotificationsByUserId(10L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Planilla procesada");
    }

    @Test
    @DisplayName("Should mark notification as read and return remaining unread count")
    void shouldMarkAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleRecord));
        when(notificationRepository.countByUserIdAndReadStatusFalse(10L)).thenReturn(0L);

        NotificationResponse response = notificationService.markAsRead(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUnreadCount()).isEqualTo(0L);
        assertThat(sampleRecord.isReadStatus()).isTrue();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when notification does not exist")
    void shouldThrowWhenNotificationNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
