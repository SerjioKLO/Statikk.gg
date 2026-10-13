package gg.statikk.notification.websocket;

import java.security.Principal;

/** Identidad de la sesión WebSocket: su "name" es el userId, y es lo que usa convertAndSendToUser. */
public class StompPrincipal implements Principal {

    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
