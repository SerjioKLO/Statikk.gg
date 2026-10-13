package gg.statikk.stats.batch;

/** Ítem de entrada del job: jugador pendiente + la versión del marcador que se leyó. */
public record PendingRecalc(Long playerProfileId, long version) {}
