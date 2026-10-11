package gg.statikk.excel.client;

/** Subconjunto de la respuesta de ms-player: lo justo para validar existencia y dueño del perfil. */
public record PlayerProfileClientResponse(
        Long id,
        Long userId,
        String displayName
) {}
