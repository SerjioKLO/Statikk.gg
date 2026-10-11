package gg.statikk.matches.event;

import java.time.Instant;

/**
 * Routing key: "match.bulk.imported". Se publica UNA vez por importación (no una por partida)
 * para que ms-stats (v0.6) recalcule las métricas del jugador una sola vez.
 */
public record MatchesBulkImportedEvent(
        String importId,
        Long playerProfileId,
        int matchesImported,
        Instant importedAt
) {}
