package com.statikk.stats.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Integer totalGamesTracked;
    private Integer grandTotalMatches;
    private Integer grandTotalWins;
    private Integer grandTotalLosses;
    private Double overallWinRate;
    private Double grandTotalHoursPlayed;
    private Double overallAverageKda;
    private List<UserGameStatsDto> gameStatsList;
}
