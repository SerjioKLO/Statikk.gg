package gg.statikk.stats.event;

/**
 * Vista mínima común a match.created, match.deleted, match.bulk.imported y match.bulk.deleted:
 * a ms-stats solo le importa QUÉ jugador cambió, no el detalle (eso lo relee de ms-matches).
 * Los demás campos del mensaje se ignoran.
 */
public record MatchesChangedEvent(Long playerProfileId) {}
