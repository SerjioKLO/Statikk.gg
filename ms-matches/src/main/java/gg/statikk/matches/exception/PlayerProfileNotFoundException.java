package gg.statikk.matches.exception;

public class PlayerProfileNotFoundException extends RuntimeException {
    public PlayerProfileNotFoundException(Long playerProfileId) {
        super("No existe el perfil de jugador " + playerProfileId + " en ms-player");
    }
}
