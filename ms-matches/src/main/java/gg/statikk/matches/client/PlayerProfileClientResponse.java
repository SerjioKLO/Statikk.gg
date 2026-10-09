package gg.statikk.matches.client;

/**
 * Subconjunto de gg.statikk.player.dto.PlayerProfileResponse — solo lo que
 * ms-matches necesita para validar que el perfil existe y a quién pertenece.
 * Duplicado a propósito (sin librería de DTOs compartida) para no acoplar
 * el deploy de ambos servicios.
 */
public record PlayerProfileClientResponse(
        Long id,
        Long userId,
        String displayName
) {}
