package gg.statikk.excel.dto;

import java.io.Serializable;
import java.time.Instant;

/** Una fila ya validada. "result" viaja como String (WIN/LOSS/DRAW) para no acoplar enums entre servicios. */
public record ImportedMatchRow(
        String gameName,
        String result,
        Integer durationMinutes,
        Integer kills,
        Integer deaths,
        Integer assists,
        Instant playedAt
) implements Serializable {}
