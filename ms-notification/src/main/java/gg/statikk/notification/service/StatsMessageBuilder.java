package gg.statikk.notification.service;

import java.util.Locale;
import java.util.Optional;

/**
 * Decide SI hay algo que avisar y QUÉ decir, comparando las métricas anteriores con las nuevas.
 * Lógica pura, sin I/O ni dependencias de Spring.
 *
 * Se usa Locale.ROOT en los formatos para que el separador decimal sea siempre "." sin importar
 * el idioma del servidor (si no, en un servidor en español saldría "60,0").
 */
public final class StatsMessageBuilder {

    private static final double EPSILON = 0.005; // las métricas ya vienen redondeadas a 2 decimales

    private StatsMessageBuilder() {
    }

    /**
     * @param previous métricas de la última notificación; null si es la primera vez que se calcula este jugador
     * @return vacío cuando nada cambió: recalcular sin cambios no debe generar ruido
     */
    public static Optional<MessageContent> build(StatsView previous, StatsView current) {
        if (previous == null) {
            if (current.totalMatches() == 0) {
                return Optional.of(new MessageContent("Estadísticas calculadas", "Tu historial de partidas está vacío."));
            }
            return Optional.of(new MessageContent("Estadísticas calculadas", String.format(Locale.ROOT,
                    "Ya tienes estadísticas: %s, %.1f%% de victorias, KDA %.2f.",
                    matches(current.totalMatches()), current.winRate(), current.kda())));
        }

        if (isSame(previous, current)) {
            return Optional.empty();
        }

        if (current.totalMatches() == 0) {
            return Optional.of(new MessageContent("Estadísticas actualizadas", "Ya no tienes partidas registradas."));
        }

        return Optional.of(new MessageContent("Estadísticas actualizadas", String.format(Locale.ROOT,
                "%s (%s), victorias %.1f%% (%s), KDA %.2f (%s).",
                matches(current.totalMatches()), deltaInt(current.totalMatches() - previous.totalMatches()),
                current.winRate(), delta(current.winRate() - previous.winRate(), 1),
                current.kda(), delta(current.kda() - previous.kda(), 2))));
    }

    private static boolean isSame(StatsView a, StatsView b) {
        return a.totalMatches() == b.totalMatches()
                && Math.abs(a.winRate() - b.winRate()) < EPSILON
                && Math.abs(a.kda() - b.kda()) < EPSILON;
    }

    private static String matches(int count) {
        return count == 1 ? "1 partida" : count + " partidas";
    }

    private static String deltaInt(int diff) {
        return diff == 0 ? "sin cambios" : String.format(Locale.ROOT, "%+d", diff);
    }

    /** Variación con signo y N decimales; si al redondear queda en 0, se dice "sin cambios" en vez de "+0.0". */
    private static String delta(double diff, int decimals) {
        double factor = Math.pow(10, decimals);
        double rounded = Math.round(diff * factor) / factor;
        if (rounded == 0) {
            return "sin cambios";
        }
        return String.format(Locale.ROOT, "%+." + decimals + "f", rounded);
    }
}
