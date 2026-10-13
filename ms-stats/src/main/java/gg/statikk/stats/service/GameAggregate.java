package gg.statikk.stats.service;

import java.io.Serializable;

/** Totales de un jugador en un videojuego. gameKey es el nombre normalizado (minúsculas, sin espacios extra). */
public record GameAggregate(
        String gameKey,
        String gameName,
        int totalMatches,
        int wins,
        int losses,
        int draws,
        long kills,
        long deaths,
        long assists,
        long totalMinutes
) implements Serializable {}
