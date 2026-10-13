package gg.statikk.notification.exception;

/** ms-player no respondió al resolver el dueño de un perfil. Lanzarla hace que RabbitMQ reintente el mensaje. */
public class PlayerServiceUnavailableException extends RuntimeException {
    public PlayerServiceUnavailableException(Long playerProfileId, Throwable cause) {
        super("ms-player no respondió al resolver el perfil " + playerProfileId, cause);
    }
}
