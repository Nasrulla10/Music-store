package com.music.musicstore.models.music;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "music")
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name cannot exceed 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "audio_file_path")
    private String audioFilePath;

    @Column(name = "original_file_name")
    private String OriginalFileName;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Music-specific fields
    @NotBlank(message = "Artist username is required")
    @Column(name = "artist_username", nullable = false)
    private String artistUsername;

    @Column(name = "album_name")
    private String albumName;

    private String genre;
    private Integer releaseYear;

    // Rating fields - better performance than calculating on-demand
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    // Flagging system for content moderation
    @Column(name = "is_flagged")
    private Boolean isFlagged = false;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt;

    @Column(name = "flagged_by_customer_id")
    private Long flaggedByCustomerId;


    // Default constructor required by JPA
    public Music() {
    }
    public Music(String name, String description, BigDecimal price,
                 String category, String artistUsername, String albumName, String genre, Integer releaseYear, String audioFilePath) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.artistUsername = artistUsername;
        this.albumName = albumName;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.audioFilePath = audioFilePath;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getOriginalFileName() { return OriginalFileName; }
    public void setOriginalFileName(String originalFileName) { this.OriginalFileName = originalFileName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getArtistUsername() { return artistUsername; }
    public void setArtistUsername(String artistUsername) { this.artistUsername = artistUsername; }

    public String getAlbumName() { return albumName; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public Boolean getIsFlagged() { return isFlagged; }
    public void setFlagged(Boolean flagged) { this.isFlagged = flagged; }

    public LocalDateTime getFlaggedAt() { return flaggedAt; }
    public void setFlaggedAt(LocalDateTime flaggedAt) { this.flaggedAt = flaggedAt; }

    public Long getFlaggedByCustomerId() { return flaggedByCustomerId; }
    public void setFlaggedByCustomerId(Long flaggedByCustomerId) { this.flaggedByCustomerId = flaggedByCustomerId; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artistUsername='" + artistUsername + '\'' +
                ", genre='" + genre + '\'' +
                ", price=" + price +
                '}';
    }
}
