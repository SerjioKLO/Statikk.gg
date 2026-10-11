package gg.statikk.matches.event;

import java.time.Instant;

/** Copia local del DTO que publica ms-excel-parser. */
public record ImportedMatchRow(
        String gameName,
        String result,
        Integer durationMinutes,
        Integer kills,
        Integer deaths,
        Integer assists,
        Instant playedAt
) {}
