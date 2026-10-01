package gg.statikk.auth.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Ya existe una cuenta registrada con el email: " + email);
    }
}
