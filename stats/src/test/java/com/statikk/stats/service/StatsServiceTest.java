package com.statikk.stats.service;

import com.statikk.stats.client.MatchServiceClient;
import com.statikk.stats.config.RabbitMQConfig;
import com.statikk.stats.dto.DashboardSummaryDto;
import com.statikk.stats.dto.MatchDto;
import com.statikk.stats.dto.UserGameStatsDto;
import com.statikk.stats.entity.UserGameStats;
import com.statikk.stats.event.StatsCalculatedEvent;
import com.statikk.stats.repository.UserGameStatsRepository;
import com.statikk.stats.service.impl.StatsServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private UserGameStatsRepository statsRepository;

    @Mock
    private MatchServiceClient matchServiceClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private StatsServiceImpl statsService;

    private UserGameStats sampleStats;

    @BeforeEach
    void setUp() {
        sampleStats = UserGameStats.builder()
                .id(1L)
                .userId(1L)
                .gameId(10L)
                .totalMatches(10)
                .totalWins(7)
                .totalLosses(3)
                .totalDraws(0)
                .winRate(70.0)
                .totalHoursPlayed(5.5)
                .totalKills(50)
                .totalDeaths(10)
                .totalAssists(30)
                .averageKda(8.0)
                .lastCalculatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("RF-05: Should return existing game stats for user")
    void shouldGetStatsByUserAndGame() {
        when(statsRepository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.of(sampleStats));

        UserGameStatsDto result = statsService.getStatsByUserAndGame(1L, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getTotalMatches()).isEqualTo(10);
        assertThat(result.getWinRate()).isEqualTo(70.0);
        assertThat(result.getAverageKda()).isEqualTo(8.0);
    }

    @Test
    @DisplayName("RF-05: Should compute grand total dashboard metrics summary for user")
    void shouldGetDashboardSummary() {
        UserGameStats game2Stats = UserGameStats.builder()
                .id(2L)
                .userId(1L)
                .gameId(20L)
                .totalMatches(10)
                .totalWins(5)
                .totalLosses(5)
                .totalDraws(0)
                .winRate(50.0)
                .totalHoursPlayed(4.5)
                .totalKills(20)
                .totalDeaths(20)
                .totalAssists(20)
                .averageKda(2.0)
                .lastCalculatedAt(LocalDateTime.now())
                .build();

        when(statsRepository.findByUserId(1L)).thenReturn(List.of(sampleStats, game2Stats));

        DashboardSummaryDto dashboard = statsService.getDashboardSummary(1L);

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getTotalGamesTracked()).isEqualTo(2);
        assertThat(dashboard.getGrandTotalMatches()).isEqualTo(20);
        assertThat(dashboard.getGrandTotalWins()).isEqualTo(12);
        assertThat(dashboard.getGrandTotalLosses()).isEqualTo(8);
        assertThat(dashboard.getOverallWinRate()).isEqualTo(60.0);
        assertThat(dashboard.getGrandTotalHoursPlayed()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("RF-04 / RF-06: Should recalculate stats from matches list and publish StatsCalculatedEvent")
    void shouldRecalculateStats() {
        MatchDto match1 = MatchDto.builder()
                .result("WIN")
                .hoursPlayed(1.0)
                .kills(10)
                .deaths(2)
                .assists(5)
                .build();

        MatchDto match2 = MatchDto.builder()
                .result("LOSS")
                .hoursPlayed(0.5)
                .kills(2)
                .deaths(4)
                .assists(2)
                .build();

        when(matchServiceClient.getMatchesByUserAndGame(1L, 10L)).thenReturn(List.of(match1, match2));
        when(statsRepository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.of(sampleStats));
        when(statsRepository.save(any(UserGameStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserGameStatsDto result = statsService.recalculateStatsForUserAndGame(1L, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getTotalMatches()).isEqualTo(2);
        assertThat(result.getTotalWins()).isEqualTo(1);
        assertThat(result.getTotalLosses()).isEqualTo(1);
        assertThat(result.getWinRate()).isEqualTo(50.0);
        assertThat(result.getTotalHoursPlayed()).isEqualTo(1.5);
        // Kills: 12, Deaths: 6, Assists: 7 => (12+7)/6 = 19/6 = 3.17
        assertThat(result.getAverageKda()).isEqualTo(3.17);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.STATS_CALCULATED_ROUTING_KEY),
                any(StatsCalculatedEvent.class)
        );
    }

    @Test
    @DisplayName("RF-07: Should delete stats for user and game")
    void shouldDeleteStats() {
        statsService.deleteStatsByUserAndGame(1L, 10L);
        verify(statsRepository, times(1)).deleteByUserIdAndGameId(1L, 10L);
    }
}
