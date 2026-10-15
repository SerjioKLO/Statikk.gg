package com.statikk.excel_parser.exception;

public class CorruptDataException extends RuntimeException {
    public CorruptDataException(String message) {
        super(message);
    }
}
