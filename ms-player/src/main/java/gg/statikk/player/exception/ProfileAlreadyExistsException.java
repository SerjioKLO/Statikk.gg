package gg.statikk.player.exception;

public class ProfileAlreadyExistsException extends RuntimeException {
    public ProfileAlreadyExistsException(Long userId) {
        super("El usuario " + userId + " ya tiene un perfil de jugador creado");
    }
}
