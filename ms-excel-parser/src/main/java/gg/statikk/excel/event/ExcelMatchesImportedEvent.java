package gg.statikk.excel.event;

import gg.statikk.excel.dto.ImportedMatchRow;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Routing key: "excel.matches.imported". Lo consume ms-matches, que inserta las partidas.
 * importId permite a quien consume detectar entregas duplicadas (RabbitMQ es "at-least-once").
 */
public record ExcelMatchesImportedEvent(
        String importId,
        Long playerProfileId,
        Long userId,
        List<ImportedMatchRow> matches,
        Instant importedAt
) implements Serializable {}
