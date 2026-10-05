package gg.statikk.player.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(Long id) {
        super("No existe un perfil de jugador con id: " + id);
    }
}
