package gg.statikk.notification.event;

import java.time.Instant;

/** Copia local del evento "stats.calculated" que publica ms-stats. */
public record StatsCalculatedEvent(
        Long playerProfileId,
        int totalMatches,
        double winRate,
        double kda,
        Instant calculatedAt
) {}
