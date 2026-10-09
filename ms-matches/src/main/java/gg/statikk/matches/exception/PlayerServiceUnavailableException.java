package gg.statikk.matches.exception;

public class PlayerServiceUnavailableException extends RuntimeException {
    public PlayerServiceUnavailableException(Long playerProfileId, Throwable cause) {
        super("ms-player no respondió al validar el perfil " + playerProfileId + " — intenta de nuevo en unos segundos", cause);
    }
}
