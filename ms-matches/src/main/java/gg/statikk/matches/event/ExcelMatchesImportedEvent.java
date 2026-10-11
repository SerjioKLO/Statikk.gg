package gg.statikk.matches.event;

import java.time.Instant;
import java.util.List;

/** Copia local del evento "excel.matches.imported" publicado por ms-excel-parser. */
public record ExcelMatchesImportedEvent(
        String importId,
        Long playerProfileId,
        Long userId,
        List<ImportedMatchRow> matches,
        Instant importedAt
) {}
