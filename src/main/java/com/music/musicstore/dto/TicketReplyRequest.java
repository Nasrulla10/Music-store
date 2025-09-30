package com.music.musicstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketReplyRequest {
    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    private String message;

    public TicketReplyRequest() {}

    public TicketReplyRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
