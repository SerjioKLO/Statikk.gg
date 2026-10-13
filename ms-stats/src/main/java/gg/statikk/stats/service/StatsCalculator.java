package gg.statikk.stats.service;

import gg.statikk.stats.client.MatchClientResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Agrupa las partidas de un jugador por videojuego y suma sus totales. Lógica pura, sin I/O. */
@Component
public class StatsCalculator {

    public List<GameAggregate> aggregate(List<MatchClientResponse> matches) {
        Map<String, Accumulator> byGame = new LinkedHashMap<>();

        for (MatchClientResponse match : matches) {
            if (match.gameName() == null || match.gameName().isBlank()) {
                continue;
            }
            String displayName = match.gameName().trim();
            // "League of Legends" y "league of legends" son el mismo juego (y la BD los trata igual).
            // ms-matches entrega lo más reciente primero, así que el nombre mostrado es el último usado.
            String key = displayName.toLowerCase(Locale.ROOT);
            byGame.computeIfAbsent(key, k -> new Accumulator(k, displayName)).add(match);
        }

        List<GameAggregate> result = new ArrayList<>(byGame.size());
        byGame.values().forEach(acc -> result.add(acc.toAggregate()));
        return result;
    }

    private static final class Accumulator {
        private final String key;
        private final String name;
        private int matches, wins, losses, draws;
        private long kills, deaths, assists, minutes;

        private Accumulator(String key, String name) {
            this.key = key;
            this.name = name;
        }

        private void add(MatchClientResponse m) {
            matches++;
            String result = m.result() == null ? "" : m.result().toUpperCase(Locale.ROOT);
            switch (result) {
                case "WIN" -> wins++;
                case "LOSS" -> losses++;
                case "DRAW" -> draws++;
                default -> { /* resultado desconocido: cuenta como partida pero no como V/D/E */ }
            }
            kills += nz(m.kills());
            deaths += nz(m.deaths());
            assists += nz(m.assists());
            minutes += nz(m.durationMinutes());
        }

        private GameAggregate toAggregate() {
            return new GameAggregate(key, name, matches, wins, losses, draws, kills, deaths, assists, minutes);
        }

        private static int nz(Integer value) {
            return value == null ? 0 : value;
        }
    }
}
