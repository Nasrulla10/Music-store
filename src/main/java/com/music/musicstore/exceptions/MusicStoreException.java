package com.music.musicstore.exceptions;

/**
 * Base exception class for Music Store application
 */
public class MusicStoreException extends RuntimeException {
    private final String errorCode;

    public MusicStoreException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
    }

    public MusicStoreException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MusicStoreException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
    }

    public MusicStoreException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
