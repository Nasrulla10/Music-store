package com.music.musicstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PlaylistDto {

    private Long id;

    @NotBlank(message = "Playlist name is required")
    @Size(max = 100, message = "Playlist name cannot exceed 100 characters")
    private String name;

    private Long customerId;
    private String customerUsername;
    private Integer trackCount;
    private String createdAt;
    private String updatedAt;

    // Default constructor
    public PlaylistDto() {
    }

    // Constructor for creating new playlist
    public PlaylistDto(String name) {
        this.name = name;
    }

    // Full constructor
    public PlaylistDto(Long id, String name, Long customerId, String customerUsername,
                      Integer trackCount, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.customerId = customerId;
        this.customerUsername = customerUsername;
        this.trackCount = trackCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public Integer getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
