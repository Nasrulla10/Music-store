package com.music.musicstore.dto;

public class ErrorResponse {
    private String message;
    private String error;
    private long timestamp;

    public ErrorResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ErrorResponse(String message, String error) {
        this.message = message;
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
