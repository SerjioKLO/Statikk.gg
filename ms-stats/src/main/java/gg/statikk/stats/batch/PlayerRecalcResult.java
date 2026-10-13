package gg.statikk.stats.batch;

import gg.statikk.stats.service.GameAggregate;

import java.util.List;

/** Ítem de salida: los totales por juego ya calculados + la versión que se debe borrar al terminar. */
public record PlayerRecalcResult(Long playerProfileId, long version, List<GameAggregate> games) {}
