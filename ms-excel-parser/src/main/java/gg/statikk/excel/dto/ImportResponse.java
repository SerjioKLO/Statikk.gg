package gg.statikk.excel.dto;

public record ImportResponse(
        String importId,
        int rowsAccepted,
        String status,
        String message
) {}
