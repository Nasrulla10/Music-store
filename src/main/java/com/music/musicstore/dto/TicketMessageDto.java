package com.music.musicstore.dto;

public class TicketMessageDto {
    private Long id;
    private String content;
    private String timestamp; // ISO 8601 UTC
    private boolean isFromStaff;
    private UserSummaryDto sender; // REQUIRED
    private UserSummaryDto staff; // optional legacy
    private UserSummaryDto customer; // optional legacy

    public TicketMessageDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isFromStaff() { return isFromStaff; }
    public void setIsFromStaff(boolean isFromStaff) { this.isFromStaff = isFromStaff; }

    public UserSummaryDto getSender() { return sender; }
    public void setSender(UserSummaryDto sender) { this.sender = sender; }

    public UserSummaryDto getStaff() { return staff; }
    public void setStaff(UserSummaryDto staff) { this.staff = staff; }

    public UserSummaryDto getCustomer() { return customer; }
    public void setCustomer(UserSummaryDto customer) { this.customer = customer; }
}

