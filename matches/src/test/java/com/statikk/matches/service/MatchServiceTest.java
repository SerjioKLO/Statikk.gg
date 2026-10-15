package com.statikk.matches.service;

import com.statikk.matches.config.RabbitMQConfig;
import com.statikk.matches.dto.CreateMatchRequest;
import com.statikk.matches.dto.MatchDto;
import com.statikk.matches.dto.MessageResponse;
import com.statikk.matches.dto.UpdateMatchRequest;
import com.statikk.matches.entity.MatchRecord;
import com.statikk.matches.event.ExcelMatchesImportedEvent;
import com.statikk.matches.event.ImportedMatchItem;
import com.statikk.matches.event.MatchesUpdatedEvent;
import com.statikk.matches.exception.ResourceNotFoundException;
import com.statikk.matches.repository.MatchRepository;
import com.statikk.matches.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MatchServiceImpl matchService;

    private MatchRecord sampleMatch;

    @BeforeEach
    void setUp() {
        sampleMatch = MatchRecord.builder()
                .id(1L)
                .userId(10L)
                .gameId(2L)
                .result("WIN")
                .durationMinutes(35.0)
                .hoursPlayed(0.58)
                .kills(12)
                .deaths(2)
                .assists(8)
                .championOrCharacter("Ahri")
                .gameMode("Ranked Solo/Duo")
                .playedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create match record and publish update event")
    void shouldCreateMatch() {
        CreateMatchRequest request = CreateMatchRequest.builder()
                .userId(10L)
                .gameId(2L)
                .result("WIN")
                .durationMinutes(35.0)
                .hoursPlayed(0.58)
                .kills(12)
                .deaths(2)
                .assists(8)
                .championOrCharacter("Ahri")
                .gameMode("Ranked Solo/Duo")
                .build();

        when(matchRepository.save(any(MatchRecord.class))).thenReturn(sampleMatch);

        MatchDto result = matchService.createMatch(request);

        assertThat(result).isNotNull();
        assertThat(result.getResult()).isEqualTo("WIN");
        assertThat(result.getKills()).isEqualTo(12);

        verify(matchRepository, times(1)).save(any(MatchRecord.class));
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.MATCHES_UPDATED_ROUTING_KEY),
                any(MatchesUpdatedEvent.class)
        );
    }

    @Test
    @DisplayName("RF-06: Should update existing match record and publish update event")
    void shouldUpdateMatch() {
        UpdateMatchRequest updateRequest = UpdateMatchRequest.builder()
                .kills(15)
                .deaths(1)
                .assists(10)
                .build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(sampleMatch));
        when(matchRepository.save(any(MatchRecord.class))).thenReturn(sampleMatch);

        MatchDto result = matchService.updateMatch(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(matchRepository, times(1)).save(sampleMatch);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.MATCHES_UPDATED_ROUTING_KEY),
                any(MatchesUpdatedEvent.class)
        );
    }

    @Test
    @DisplayName("RF-04: Should process and bulk save imported matches from Excel")
    void shouldProcessImportedMatches() {
        ImportedMatchItem item1 = ImportedMatchItem.builder()
                .result("WIN")
                .hoursPlayed(0.6)
                .kills(8)
                .deaths(1)
                .assists(11)
                .championOrCharacter("Jinx")
                .playedAt(LocalDateTime.now())
                .build();

        ExcelMatchesImportedEvent event = ExcelMatchesImportedEvent.builder()
                .userId(10L)
                .gameId(2L)
                .filename("matches_2026.xlsx")
                .matches(List.of(item1))
                .timestamp(LocalDateTime.now())
                .build();

        when(matchRepository.saveAll(anyList())).thenReturn(List.of(sampleMatch));

        int processed = matchService.processImportedMatches(event);

        assertThat(processed).isEqualTo(1);
        verify(matchRepository, times(1)).saveAll(anyList());
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.MATCHES_UPDATED_ROUTING_KEY),
                any(MatchesUpdatedEvent.class)
        );
    }

    @Test
    @DisplayName("RF-07: Should delete all matches for user and game when game is deleted")
    void shouldDeleteMatchesByUserAndGame() {
        when(matchRepository.countByUserIdAndGameId(10L, 2L)).thenReturn(5L);

        MessageResponse response = matchService.deleteMatchesByUserAndGame(10L, 2L);

        assertThat(response).isNotNull();
        assertThat(response.getCount()).isEqualTo(5);
        verify(matchRepository, times(1)).deleteByUserIdAndGameId(10L, 2L);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.MATCHES_UPDATED_ROUTING_KEY),
                any(MatchesUpdatedEvent.class)
        );
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when match does not exist")
    void shouldThrowWhenMatchNotFound() {
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getMatchById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
