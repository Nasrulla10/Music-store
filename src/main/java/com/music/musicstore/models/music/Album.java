package com.music.musicstore.models.music;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Album title is required")
    @Size(max = 100, message = "Album title cannot exceed 100 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @Column(name = "artist")
    private String artistUsername;

    @Column(name = "artist_username")
    private String artistUsernameField;

    @NotBlank(message = "Genre is required")
    @Column(nullable = false)
    private String genre;

    @Column(name = "cover")
    private String cover;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship with Music tracks - removed to avoid circular reference issues

    // Default constructor
    public Album() {}

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

    public String getArtistUsername() {
        return artistUsername;
    }

    public void setArtistUsername(String artistUsername) {
        this.artistUsername = artistUsername;
        this.artistUsernameField = artistUsername; // Keep both fields in sync
    }

    public String getArtistUsernameField() {
        return artistUsernameField;
    }

    public void setArtistUsernameField(String artistUsernameField) {
        this.artistUsernameField = artistUsernameField;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
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

    // Tracks methods removed - use separate service to get tracks by album name

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
