package com.music.musicstore.repositories;

import com.music.musicstore.models.music.Review;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.users.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByMusicOrderByCreatedAtDesc(Music music, Pageable pageable);

    List<Review> findByMusicOrderByCreatedAtDesc(Music music);

    Optional<Review> findByMusicAndCustomer(Music music, Customer customer);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.music = :music")
    Double findAverageRatingByMusic(@Param("music") Music music);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.music = :music")
    Long countReviewsByMusic(@Param("music") Music music);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.music = :music AND r.rating = :rating")
    Long countReviewsByMusicAndRating(@Param("music") Music music, @Param("rating") Integer rating);

    // NEW: Missing methods for admin review management and analytics
    long countByRating(Integer rating);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getAverageRating();

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Review> findByMusic(Music music);
}
