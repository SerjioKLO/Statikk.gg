package com.statikk.stats.controller;

import com.statikk.stats.dto.DashboardSummaryDto;
import com.statikk.stats.dto.UserGameStatsDto;
import com.statikk.stats.service.StatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @MockBean
    private JobLauncher jobLauncher;

    @MockBean
    private Job aggregateStatsJob;

    @Test
    @DisplayName("GET /api/stats/user/{userId}/game/{gameId} - RF-05: Should return game stats")
    void shouldGetStatsByUserAndGame() throws Exception {
        UserGameStatsDto dto = UserGameStatsDto.builder()
                .userId(1L)
                .gameId(2L)
                .totalMatches(10)
                .totalWins(8)
                .winRate(80.0)
                .totalHoursPlayed(6.0)
                .build();

        when(statsService.getStatsByUserAndGame(1L, 2L)).thenReturn(dto);

        mockMvc.perform(get("/api/stats/user/1/game/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMatches").value(10))
                .andExpect(jsonPath("$.winRate").value(80.0))
                .andExpect(jsonPath("$.totalHoursPlayed").value(6.0));
    }

    @Test
    @DisplayName("GET /api/stats/dashboard/{userId} - RF-05: Should return centralized dashboard summary")
    void shouldGetDashboardSummary() throws Exception {
        DashboardSummaryDto dashboard = DashboardSummaryDto.builder()
                .userId(1L)
                .totalGamesTracked(2)
                .grandTotalMatches(30)
                .overallWinRate(66.7)
                .grandTotalHoursPlayed(25.0)
                .build();

        when(statsService.getDashboardSummary(1L)).thenReturn(dashboard);

        mockMvc.perform(get("/api/stats/dashboard/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGamesTracked").value(2))
                .andExpect(jsonPath("$.grandTotalMatches").value(30))
                .andExpect(jsonPath("$.overallWinRate").value(66.7));
    }

    @Test
    @DisplayName("POST /api/stats/recalculate/user/{userId}/game/{gameId} - RF-06: Should recalculate stats")
    void shouldRecalculateStats() throws Exception {
        UserGameStatsDto dto = UserGameStatsDto.builder()
                .userId(1L)
                .gameId(2L)
                .totalMatches(15)
                .build();

        when(statsService.recalculateStatsForUserAndGame(1L, 2L)).thenReturn(dto);

        mockMvc.perform(post("/api/stats/recalculate/user/1/game/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMatches").value(15));
    }
}
