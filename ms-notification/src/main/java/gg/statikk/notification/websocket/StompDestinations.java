package gg.statikk.notification.websocket;

/** Reglas de qué destinos STOMP puede usar un cliente. Sin dependencias de Spring para poder probarlas aisladas. */
public final class StompDestinations {

    /** Lo único a lo que un cliente puede suscribirse: su propia cola personal (/user/queue/...). */
    public static final String ALLOWED_SUBSCRIPTION_PREFIX = "/user/queue/";

    private StompDestinations() {
    }

    public static boolean isAllowedSubscription(String destination) {
        return destination != null
                && destination.startsWith(ALLOWED_SUBSCRIPTION_PREFIX)
                && !destination.contains("..");
    }
}
