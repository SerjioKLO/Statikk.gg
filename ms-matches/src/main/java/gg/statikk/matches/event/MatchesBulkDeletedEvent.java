package gg.statikk.matches.event;

import java.time.Instant;

/**
 * Routing key: "match.bulk.deleted". Se publica DESPUÉS de borrar las partidas, para que ms-stats
 * recalcule recién cuando ya no existen (si escuchara player.game.deleted directamente, podría
 * recalcular antes de que ms-matches termine de borrar y dejar el juego "vivo" en las métricas).
 */
public record MatchesBulkDeletedEvent(
        Long playerProfileId,
        String gameName,
        int matchesDeleted,
        Instant deletedAt
) {}
