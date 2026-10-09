package gg.statikk.matches.event;

import java.time.Instant;

/** Routing key: "match.deleted". */
public record MatchDeletedEvent(
        Long matchId,
        Long playerProfileId,
        Instant deletedAt
) {}
