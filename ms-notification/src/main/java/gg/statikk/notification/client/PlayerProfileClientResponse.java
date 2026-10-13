package gg.statikk.notification.client;

/** Subconjunto de la respuesta de ms-player: se necesita el userId para saber a QUIÉN notificar. */
public record PlayerProfileClientResponse(
        Long id,
        Long userId,
        String displayName
) {}
