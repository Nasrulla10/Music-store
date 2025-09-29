package com.music.musicstore.exceptions;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends MusicStoreException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    public ValidationException(String field, String value) {
        super(String.format("Validation failed for field '%s' with value '%s'", field, value), "VALIDATION_ERROR");
    }
}
