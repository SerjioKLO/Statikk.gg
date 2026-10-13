package gg.statikk.matches.event;

import java.time.Instant;

/** Copia local del evento "player.game.deleted" que publica ms-player. */
public record PlayerGameDeletedEvent(
        Long playerProfileId,
        Long userId,
        Long gameId,
        String gameName,
        Instant deletedAt
) {}
