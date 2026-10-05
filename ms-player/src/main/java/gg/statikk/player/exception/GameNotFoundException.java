package gg.statikk.player.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(Long gameId, Long playerProfileId) {
        super("No existe el videojuego " + gameId + " para el perfil " + playerProfileId);
    }
}
