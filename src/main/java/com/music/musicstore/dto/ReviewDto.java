package com.music.musicstore.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;
    private Long musicId;
    private String customerUsername;
    private String customerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isOwnReview;

    public ReviewDto() {}

    public ReviewDto(Long id, Long musicId, String customerUsername, String customerName,
                    Integer rating, String comment, LocalDateTime createdAt,
                    LocalDateTime updatedAt, boolean isOwnReview) {
        this.id = id;
        this.musicId = musicId;
        this.customerUsername = customerUsername;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isOwnReview = isOwnReview;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMusicId() { return musicId; }
    public void setMusicId(Long musicId) { this.musicId = musicId; }

    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isOwnReview() { return isOwnReview; }
    public void setOwnReview(boolean ownReview) { isOwnReview = ownReview; }
}
