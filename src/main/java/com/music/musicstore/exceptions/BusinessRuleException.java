package com.music.musicstore.exceptions;

/**
 * Exception thrown when a business rule is violated
 */
public class BusinessRuleException extends MusicStoreException {
    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }
}
