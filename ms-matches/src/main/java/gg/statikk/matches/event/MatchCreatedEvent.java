package gg.statikk.matches.event;

import java.time.Instant;

/** Routing key: "match.created". Lo consumirá ms-stats (v0.6) para recalcular métricas. */
public record MatchCreatedEvent(
        Long matchId,
        Long playerProfileId,
        String gameName,
        Instant createdAt
) {}
