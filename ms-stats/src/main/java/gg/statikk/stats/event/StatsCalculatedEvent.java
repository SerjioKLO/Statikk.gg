package gg.statikk.stats.event;

import java.time.Instant;

/** Routing key: "stats.calculated". Lo consumirá ms-notification (v0.7) para avisar al jugador. */
public record StatsCalculatedEvent(
        Long playerProfileId,
        int totalMatches,
        double winRate,
        double kda,
        Instant calculatedAt
) {}
