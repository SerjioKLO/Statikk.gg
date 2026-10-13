package gg.statikk.stats.service;

import gg.statikk.stats.dto.StatsSummary;

import java.util.List;

/** Fórmulas de las métricas derivadas. Una sola implementación para no divergir entre el batch y la consulta. */
public final class StatsMath {

    private StatsMath() {
    }

    public static StatsSummary summarize(GameAggregate g) {
        double winRate = g.totalMatches() == 0 ? 0.0 : g.wins() * 100.0 / g.totalMatches();
        double winLossRatio = (double) g.wins() / Math.max(g.losses(), 1);
        double kda = (double) (g.kills() + g.assists()) / Math.max(g.deaths(), 1);
        double hours = g.totalMinutes() / 60.0;

        return new StatsSummary(
                g.totalMatches(), g.wins(), g.losses(), g.draws(),
                round2(winRate), round2(winLossRatio), round2(kda),
                g.kills(), g.deaths(), g.assists(), round2(hours));
    }

    /** Suma todos los videojuegos del jugador y calcula las métricas globales sobre los totales (no promedio de promedios). */
    public static StatsSummary overall(List<GameAggregate> games) {
        int matches = 0, wins = 0, losses = 0, draws = 0;
        long kills = 0, deaths = 0, assists = 0, minutes = 0;
        for (GameAggregate g : games) {
            matches += g.totalMatches();
            wins += g.wins();
            losses += g.losses();
            draws += g.draws();
            kills += g.kills();
            deaths += g.deaths();
            assists += g.assists();
            minutes += g.totalMinutes();
        }
        return summarize(new GameAggregate("*", "*", matches, wins, losses, draws, kills, deaths, assists, minutes));
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
