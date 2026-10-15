package com.statikk.notification.consumer;

import com.statikk.notification.dto.NotificationDto;
import com.statikk.notification.event.ExcelMatchesImportedEvent;
import com.statikk.notification.event.MatchesUpdatedEvent;
import com.statikk.notification.event.StatsCalculatedEvent;
import com.statikk.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventConsumer consumer;

    @Test
    @DisplayName("Should handle ExcelMatchesImportedEvent and send notification")
    void shouldHandleExcelImported() {
        ExcelMatchesImportedEvent event = ExcelMatchesImportedEvent.builder()
                .userId(1L)
                .gameId(2L)
                .filename("test.xlsx")
                .timestamp(LocalDateTime.now())
                .build();

        when(notificationService.createAndSendNotification(eq(1L), anyString(), anyString(), eq("FILE_PROCESSED")))
                .thenReturn(NotificationDto.builder().build());

        consumer.handleExcelImported(event);

        verify(notificationService, times(1))
                .createAndSendNotification(eq(1L), anyString(), anyString(), eq("FILE_PROCESSED"));
    }

    @Test
    @DisplayName("Should handle StatsCalculatedEvent and send notification")
    void shouldHandleStatsCalculated() {
        StatsCalculatedEvent event = StatsCalculatedEvent.builder()
                .userId(1L)
                .gameId(2L)
                .totalMatches(10)
                .winRate(70.0)
                .totalHoursPlayed(5.0)
                .averageKda(4.0)
                .timestamp(LocalDateTime.now())
                .build();

        when(notificationService.createAndSendNotification(eq(1L), anyString(), anyString(), eq("STATS_UPDATED")))
                .thenReturn(NotificationDto.builder().build());

        consumer.handleStatsCalculated(event);

        verify(notificationService, times(1))
                .createAndSendNotification(eq(1L), anyString(), anyString(), eq("STATS_UPDATED"));
    }

    @Test
    @DisplayName("Should handle MatchesUpdatedEvent and send notification")
    void shouldHandleMatchesUpdated() {
        MatchesUpdatedEvent event = MatchesUpdatedEvent.builder()
                .userId(1L)
                .gameId(2L)
                .eventType("CREATED")
                .matchCount(1)
                .timestamp(LocalDateTime.now())
                .build();

        when(notificationService.createAndSendNotification(eq(1L), anyString(), anyString(), eq("MATCHES_MODIFIED")))
                .thenReturn(NotificationDto.builder().build());

        consumer.handleMatchesUpdated(event);

        verify(notificationService, times(1))
                .createAndSendNotification(eq(1L), anyString(), anyString(), eq("MATCHES_MODIFIED"));
    }
}
