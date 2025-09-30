package com.music.musicstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MusicDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String audioFilePath;
    private String originalFileName;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Music-specific fields
    private String artist;
    private String artistUsername;
    private String album;
    private String albumName;
    private String genre;
    private Integer releaseYear;

    // Rating fields
    private Double averageRating;
    private Integer totalReviews;

    // Status fields
    private Boolean isFlagged;
    private LocalDateTime flaggedAt;

    // Default constructor
    public MusicDto() {}

    // Constructor with essential fields
    public MusicDto(Long id, String name, String description, BigDecimal price,
                    String artist, String genre, String imageUrl, String audioFilePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.artist = artist;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.audioFilePath = audioFilePath;
    }

    // Constructor matching MusicApiController usage (14 parameters)
    public MusicDto(Long id, String name, String description, BigDecimal price,
                    String imageUrl, String audioFilePath, String category,
                    String artist, String album, String genre, Integer releaseYear,
                    LocalDateTime createdAt, double averageRating, Integer totalReviews) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.audioFilePath = audioFilePath;
        this.category = category;
        this.artist = artist;
        this.artistUsername = artist; // Set both artist and artistUsername
        this.album = album;
        this.albumName = album; // Set both album and albumName
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.createdAt = createdAt;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAudioFilePath() { return audioFilePath; }
    public void setAudioFilePath(String audioFilePath) { this.audioFilePath = audioFilePath; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getArtistUsername() { return artistUsername; }
    public void setArtistUsername(String artistUsername) { this.artistUsername = artistUsername; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getAlbumName() { return albumName; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public Boolean getIsFlagged() { return isFlagged; }
    public void setIsFlagged(Boolean isFlagged) { this.isFlagged = isFlagged; }

    public LocalDateTime getFlaggedAt() { return flaggedAt; }
    public void setFlaggedAt(LocalDateTime flaggedAt) { this.flaggedAt = flaggedAt; }
}
