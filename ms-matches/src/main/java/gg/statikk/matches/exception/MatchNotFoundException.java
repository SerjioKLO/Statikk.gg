package gg.statikk.matches.exception;

public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(Long id) {
        super("No existe una partida con id: " + id);
    }
}
