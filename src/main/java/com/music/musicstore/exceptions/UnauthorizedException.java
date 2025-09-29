package com.music.musicstore.exceptions;

/**
 * Exception thrown when authorization fails
 */
public class UnauthorizedException extends MusicStoreException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }
}
