package gg.statikk.excel.dto;

/** Error de validación ubicado: fila (la cabecera es la 1), columna y motivo. */
public record RowError(
        int row,
        String column,
        String message
) {}
