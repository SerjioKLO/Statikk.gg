package gg.statikk.player.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Publicado en el exchange "statikk.events" con routing key "player.game.deleted".
 * ms-stats (v0.6) lo consumirá para recalcular las métricas agregadas del jugador.
 */
public record PlayerGameDeletedEvent(
        Long playerProfileId,
        Long userId,
        Long gameId,
        String gameName,
        Instant deletedAt
) implements Serializable {}
