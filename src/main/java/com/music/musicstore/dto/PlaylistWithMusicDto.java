 package com.music.musicstore.dto;

import com.music.musicstore.models.music.Music;
import java.util.List;

public class PlaylistWithMusicDto {

    private Long id;
    private String name;
    private Long customerId;
    private String customerUsername;
    private Integer trackCount;
    private String createdAt;
    private String updatedAt;
    private List<MusicDto> musics;

    // Default constructor
    public PlaylistWithMusicDto() {
    }

    // Full constructor
    public PlaylistWithMusicDto(Long id, String name, Long customerId, String customerUsername,
                                Integer trackCount, String createdAt, String updatedAt, List<MusicDto> musics) {
        this.id = id;
        this.name = name;
        this.customerId = customerId;
        this.customerUsername = customerUsername;
        this.trackCount = trackCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.musics = musics;
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

    public List<MusicDto> getMusics() {
        return musics;
    }

    public void setMusics(List<MusicDto> musics) {
        this.musics = musics;
    }
}
