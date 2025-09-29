package com.music.musicstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlbumDto {
    private Long id;
    private String title;
    private String description;
    private String artist;
    private String genre;
    private BigDecimal price;
    private String coverImageUrl;
    private LocalDateTime releaseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int trackCount;

    // Default constructor
    public AlbumDto() {}

    // Constructor
    public AlbumDto(Long id, String title, String description, String artist,
                   String genre, BigDecimal price, String coverImageUrl,
                   LocalDateTime releaseDate, LocalDateTime createdAt,
                   LocalDateTime updatedAt, int trackCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.artist = artist;
        this.genre = genre;
        this.price = price;
        this.coverImageUrl = coverImageUrl;
        this.releaseDate = releaseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.trackCount = trackCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }
}
