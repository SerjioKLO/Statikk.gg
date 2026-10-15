package com.statikk.matches.consumer;

import com.statikk.matches.dto.MessageResponse;
import com.statikk.matches.event.ExcelMatchesImportedEvent;
import com.statikk.matches.event.GameDeletedEvent;
import com.statikk.matches.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchEventConsumerTest {

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchEventConsumer matchEventConsumer;

    @Test
    @DisplayName("Should consume ExcelMatchesImportedEvent and call processImportedMatches")
    void shouldHandleExcelMatchesImported() {
        ExcelMatchesImportedEvent event = ExcelMatchesImportedEvent.builder()
                .userId(1L)
                .gameId(2L)
                .matches(Collections.emptyList())
                .build();

        when(matchService.processImportedMatches(event)).thenReturn(0);

        matchEventConsumer.handleExcelMatchesImported(event);

        verify(matchService, times(1)).processImportedMatches(event);
    }

    @Test
    @DisplayName("Should consume GameDeletedEvent and call deleteMatchesByUserAndGame")
    void shouldHandleGameDeleted() {
        GameDeletedEvent event = GameDeletedEvent.builder()
                .userId(1L)
                .gameId(2L)
                .gameName("Valorant")
                .timestamp(LocalDateTime.now())
                .build();

        when(matchService.deleteMatchesByUserAndGame(1L, 2L)).thenReturn(MessageResponse.builder().build());

        matchEventConsumer.handleGameDeleted(event);

        verify(matchService, times(1)).deleteMatchesByUserAndGame(1L, 2L);
    }
}
