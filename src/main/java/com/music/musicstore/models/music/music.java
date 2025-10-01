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
