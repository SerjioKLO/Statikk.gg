package gg.statikk.stats.dto;

import java.io.Serializable;

public record GameStatsResponse(
        String gameName,
        StatsSummary stats
) implements Serializable {}
