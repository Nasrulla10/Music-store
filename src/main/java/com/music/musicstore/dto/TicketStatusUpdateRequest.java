package com.music.musicstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TicketStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(OPEN|IN_PROGRESS|URGENT|CLOSED|RESOLVED)$",
             message = "Status must be one of: OPEN, IN_PROGRESS, URGENT, CLOSED, RESOLVED")
    private String status;

    public TicketStatusUpdateRequest() {}

    public TicketStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
