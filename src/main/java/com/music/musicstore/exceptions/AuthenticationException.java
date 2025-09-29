package com.music.musicstore.exceptions;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends MusicStoreException {
    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }
}
