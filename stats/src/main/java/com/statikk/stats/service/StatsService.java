package com.statikk.stats.service;

import com.statikk.stats.dto.DashboardSummaryDto;
import com.statikk.stats.dto.UserGameStatsDto;

import java.util.List;

public interface StatsService {
    UserGameStatsDto getStatsByUserAndGame(Long userId, Long gameId);
    List<UserGameStatsDto> getStatsByUserId(Long userId);
    DashboardSummaryDto getDashboardSummary(Long userId);
    UserGameStatsDto recalculateStatsForUserAndGame(Long userId, Long gameId);
    void deleteStatsByUserAndGame(Long userId, Long gameId);
    void recalculateAllStats();
}
