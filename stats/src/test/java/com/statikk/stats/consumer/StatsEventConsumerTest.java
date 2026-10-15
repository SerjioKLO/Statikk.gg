package com.statikk.stats.consumer;

import com.statikk.stats.dto.UserGameStatsDto;
import com.statikk.stats.event.GameDeletedEvent;
import com.statikk.stats.event.MatchesUpdatedEvent;
import com.statikk.stats.service.StatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsEventConsumerTest {

    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsEventConsumer consumer;

    @Test
    @DisplayName("Should handle MatchesUpdatedEvent and recalculate stats")
    void shouldHandleMatchesUpdated() {
        MatchesUpdatedEvent event = MatchesUpdatedEvent.builder()
                .userId(1L)
                .gameId(2L)
                .eventType("IMPORTED")
                .matchCount(5)
                .timestamp(LocalDateTime.now())
                .build();

        when(statsService.recalculateStatsForUserAndGame(1L, 2L)).thenReturn(UserGameStatsDto.builder().build());

        consumer.handleMatchesUpdated(event);

        verify(statsService, times(1)).recalculateStatsForUserAndGame(1L, 2L);
    }

    @Test
    @DisplayName("Should handle GameDeletedEvent and remove stats")
    void shouldHandleGameDeleted() {
        GameDeletedEvent event = GameDeletedEvent.builder()
                .userId(1L)
                .gameId(2L)
                .gameName("Overwatch")
                .timestamp(LocalDateTime.now())
                .build();

        consumer.handleGameDeleted(event);

        verify(statsService, times(1)).deleteStatsByUserAndGame(1L, 2L);
    }
}
