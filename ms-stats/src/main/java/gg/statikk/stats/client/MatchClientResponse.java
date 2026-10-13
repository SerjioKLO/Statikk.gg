package gg.statikk.stats.client;

import java.time.Instant;

/** Subconjunto de la respuesta de ms-matches: lo necesario para agregar métricas. */
public record MatchClientResponse(
        Long id,
        Long playerProfileId,
        String gameName,
        String result,
        Integer durationMinutes,
        Integer kills,
        Integer deaths,
        Integer assists,
        Instant playedAt
) {}
