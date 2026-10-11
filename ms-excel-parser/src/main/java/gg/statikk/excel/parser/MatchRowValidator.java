package gg.statikk.excel.parser;

import gg.statikk.excel.dto.ImportedMatchRow;
import gg.statikk.excel.dto.RowError;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Valida una fila y acumula TODOS sus errores (no se detiene en el primero). */
@Component
public class MatchRowValidator {

    private static final Set<String> RESULTS = Set.of("WIN", "LOSS", "DRAW");
    private static final DateTimeFormatter SPACE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]");

    /** Devuelve la fila validada, o null si tuvo errores (que quedan agregados a "errors"). */
    public ImportedMatchRow validate(RawRow raw, List<RowError> errors) {
        int errorsBefore = errors.size();
        List<String> cells = raw.cells();

        String gameName = cells.get(0);
        if (gameName.isBlank()) {
            errors.add(new RowError(raw.number(), "gameName", "es obligatorio"));
        } else if (gameName.length() > 100) {
            errors.add(new RowError(raw.number(), "gameName", "máximo 100 caracteres"));
        }

        String result = cells.get(1).toUpperCase(Locale.ROOT);
        if (!RESULTS.contains(result)) {
            errors.add(new RowError(raw.number(), "result",
                    "debe ser WIN, LOSS o DRAW (recibido: '" + cells.get(1) + "')"));
        }

        Integer duration = parseNonNegativeInt(raw, 2, "durationMinutes", null, errors);
        Integer kills = parseNonNegativeInt(raw, 3, "kills", 0, errors);
        Integer deaths = parseNonNegativeInt(raw, 4, "deaths", 0, errors);
        Integer assists = parseNonNegativeInt(raw, 5, "assists", 0, errors);
        Instant playedAt = parseDate(raw, errors);

        if (errors.size() > errorsBefore) {
            return null;
        }
        return new ImportedMatchRow(gameName, result, duration, kills, deaths, assists, playedAt);
    }

    private Integer parseNonNegativeInt(RawRow raw, int index, String column, Integer defaultValue, List<RowError> errors) {
        String value = raw.cells().get(index);
        if (value.isBlank()) {
            return defaultValue;
        }
        try {
            int number = Integer.parseInt(value);
            if (number < 0) {
                errors.add(new RowError(raw.number(), column, "no puede ser negativo (recibido: " + number + ")"));
                return null;
            }
            return number;
        } catch (NumberFormatException ex) {
            errors.add(new RowError(raw.number(), column, "debe ser un número entero (recibido: '" + value + "')"));
            return null;
        }
    }

    /** Acepta ISO instant, ISO local datetime, "yyyy-MM-dd HH:mm[:ss]" y "yyyy-MM-dd". Vacío -> null. */
    private Instant parseDate(RawRow raw, List<RowError> errors) {
        String value = raw.cells().get(6);
        if (value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            // probamos el siguiente formato
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // probamos el siguiente formato
        }
        try {
            return LocalDateTime.parse(value, SPACE_DATE_TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // probamos el siguiente formato
        }
        try {
            return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            errors.add(new RowError(raw.number(), "playedAt",
                    "fecha inválida (usa 2026-10-11T18:30:00Z, 2026-10-11 18:30 o 2026-10-11; recibido: '" + value + "')"));
            return null;
        }
    }
}
