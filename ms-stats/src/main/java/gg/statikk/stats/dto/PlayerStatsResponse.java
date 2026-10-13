package gg.statikk.stats.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record PlayerStatsResponse(
        Long playerProfileId,
        StatsSummary overall,
        List<GameStatsResponse> games,
        Instant calculatedAt   // null si todavía no se calculó nada para este jugador
) implements Serializable {}
