package gg.statikk.excel.exception;

import gg.statikk.excel.dto.RowError;

import java.util.List;

public class InvalidFileException extends RuntimeException {

    private static final int MAX_ERRORS_REPORTED = 50;

    private final List<RowError> errors;
    private final int totalErrors;

    public InvalidFileException(String message, List<RowError> errors) {
        super(message);
        this.totalErrors = errors.size();
        this.errors = errors.stream().limit(MAX_ERRORS_REPORTED).toList();
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public int getTotalErrors() {
        return totalErrors;
    }
}
