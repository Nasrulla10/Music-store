package com.music.musicstore.exceptions;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends MusicStoreException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier), "RESOURCE_NOT_FOUND");
    }
}
