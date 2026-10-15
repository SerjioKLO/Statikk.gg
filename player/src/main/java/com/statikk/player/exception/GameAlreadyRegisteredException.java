package com.statikk.player.exception;

public class GameAlreadyRegisteredException extends RuntimeException {
    public GameAlreadyRegisteredException(String message) {
        super(message);
    }
}
