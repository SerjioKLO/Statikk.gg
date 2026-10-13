package gg.statikk.stats.dto;

import java.io.Serializable;

public record StatsSummary(
        int totalMatches,
        int wins,
        int losses,
        int draws,
        double winRate,        // % de victorias sobre el total de partidas
        double winLossRatio,   // victorias / derrotas (con 0 derrotas se usa 1)
        double kda,            // (kills + assists) / muertes (con 0 muertes se usa 1)
        long totalKills,
        long totalDeaths,
        long totalAssists,
        double hoursPlayed
) implements Serializable {}
