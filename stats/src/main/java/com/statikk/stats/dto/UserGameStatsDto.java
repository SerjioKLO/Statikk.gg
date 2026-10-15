package com.statikk.stats.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGameStatsDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long gameId;
    private Integer totalMatches;
    private Integer totalWins;
    private Integer totalLosses;
    private Integer totalDraws;
    private Double winRate;
    private Double totalHoursPlayed;
    private Integer totalKills;
    private Integer totalDeaths;
    private Integer totalAssists;
    private Double averageKda;
    private LocalDateTime lastCalculatedAt;
}
