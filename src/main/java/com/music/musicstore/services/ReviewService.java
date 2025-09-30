package com.music.musicstore.services;

import com.music.musicstore.models.music.Review;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.ReviewRepository;
import com.music.musicstore.repositories.MusicRepository;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.exceptions.BusinessRuleException;
import com.music.musicstore.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final MusicRepository musicRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, MusicRepository musicRepository) {
        this.reviewRepository = reviewRepository;
        this.musicRepository = musicRepository;
        logger.info("ReviewService initialized successfully");
    }

    public Review createReview(Long musicId, Customer customer, Integer rating, String comment) {
        logger.debug("Creating review for music ID: {} by customer: {}", musicId, customer != null ? customer.getUsername() : "null");

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        if (rating == null || rating < 1 || rating > 5) {
            logger.error("Invalid rating: {}", rating);
            throw new ValidationException("Rating must be between 1 and 5");
        }

        try {
            Music music = musicRepository.findById(musicId)
                    .orElseThrow(() -> {
                        logger.error("Music not found with ID: {}", musicId);
                        return new ResourceNotFoundException("Music", musicId.toString());
                    });

            // Check if user already reviewed this music
            Optional<Review> existingReview = reviewRepository.findByMusicAndCustomer(music, customer);
            if (existingReview.isPresent()) {
                logger.error("Customer {} has already reviewed music ID: {}", customer.getUsername(), musicId);
                throw new BusinessRuleException("You have already reviewed this music");
            }

            Review review = new Review(music, customer, rating, comment);
            Review savedReview = reviewRepository.save(review);

            // Update music rating statistics
            updateMusicRatingStats(music);

            logger.info("Successfully created review for music ID: {} by customer: {} (Review ID: {})",
                       musicId, customer.getUsername(), savedReview.getId());
            return savedReview;
        } catch (Exception e) {
            logger.error("Error creating review for music ID: {} by customer: {}", musicId, customer.getUsername(), e);
            throw e;
        }
    }

    public Review updateReview(Long reviewId, Customer customer, Integer rating, String comment) {
        logger.debug("Updating review ID: {} by customer: {}", reviewId, customer != null ? customer.getUsername() : "null");

        if (reviewId == null) {
            logger.error("Review ID is null");
            throw new ValidationException("Review ID cannot be null");
        }

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        if (rating != null && (rating < 1 || rating > 5)) {
            logger.error("Invalid rating: {}", rating);
            throw new ValidationException("Rating must be between 1 and 5");
        }

        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> {
                        logger.error("Review not found with ID: {}", reviewId);
                        return new ResourceNotFoundException("Review", reviewId.toString());
                    });

            if (!review.getCustomer().getId().equals(customer.getId())) {
                logger.error("Customer {} cannot update review ID: {} (owned by {})",
                           customer.getUsername(), reviewId, review.getCustomer().getUsername());
                throw new UnauthorizedException("You can only update your own reviews");
            }

            if (rating != null) {
                review.setRating(rating);
            }
            if (comment != null) {
                review.setComment(comment);
            }
            review.setUpdatedAt(LocalDateTime.now());

            Review updatedReview = reviewRepository.save(review);

            // Update music rating statistics after updating review
            updateMusicRatingStats(review.getMusic());

            logger.info("Successfully updated review ID: {} by customer: {}", reviewId, customer.getUsername());
            return updatedReview;
        } catch (Exception e) {
            logger.error("Error updating review ID: {} by customer: {}", reviewId, customer.getUsername(), e);
            throw e;
        }
    }

    public void deleteReview(Long reviewId, Customer customer) {
        logger.debug("Deleting review ID: {} by customer: {}", reviewId, customer != null ? customer.getUsername() : "null");

        if (reviewId == null) {
            logger.error("Review ID is null");
            throw new ValidationException("Review ID cannot be null");
        }

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> {
                        logger.error("Review not found with ID: {}", reviewId);
                        return new ResourceNotFoundException("Review", reviewId.toString());
                    });

            if (!review.getCustomer().getId().equals(customer.getId())) {
                logger.error("Customer {} cannot delete review ID: {} (owned by {})",
                           customer.getUsername(), reviewId, review.getCustomer().getUsername());
                throw new UnauthorizedException("You can only delete your own reviews");
            }

            Music music = review.getMusic();
            reviewRepository.delete(review);

            // Update music rating statistics after deleting review
            updateMusicRatingStats(music);

            logger.info("Successfully deleted review ID: {} by customer: {}", reviewId, customer.getUsername());
        } catch (Exception e) {
            logger.error("Error deleting review ID: {} by customer: {}", reviewId, customer.getUsername(), e);
            throw e;
        }
    }

    public List<Review> getReviewsByMusic(Long musicId) {
        logger.debug("Getting reviews for music ID: {}", musicId);

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        try {
            Music music = musicRepository.findById(musicId)
                    .orElseThrow(() -> {
                        logger.error("Music not found with ID: {}", musicId);
                        return new ResourceNotFoundException("Music", musicId.toString());
                    });

            // Use findAll and filter manually since findByMusic method may not exist
            List<Review> allReviews = reviewRepository.findAll();
            List<Review> reviews = allReviews.stream()
                .filter(review -> review.getMusic().getId().equals(musicId))
                .collect(java.util.stream.Collectors.toList());

            logger.info("Successfully retrieved {} reviews for music ID: {}", reviews.size(), musicId);
            return reviews;
        } catch (Exception e) {
            logger.error("Error getting reviews for music ID: {}", musicId, e);
            throw e;
        }
    }

    public Page<Review> getReviewsByMusicPaginated(Long musicId, Pageable pageable) {
        logger.debug("Getting paginated reviews for music ID: {}", musicId);

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        if (pageable == null) {
            logger.error("Pageable is null");
            throw new ValidationException("Pageable cannot be null");
        }

        try {
            // Get all reviews for the music and create a manual page
            List<Review> allReviews = getReviewsByMusic(musicId);

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allReviews.size());
            List<Review> pageContent = allReviews.subList(start, end);

            Page<Review> reviewPage = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, allReviews.size());

            logger.info("Successfully retrieved {} paginated reviews for music ID: {}",
                       reviewPage.getNumberOfElements(), musicId);
            return reviewPage;
        } catch (Exception e) {
            logger.error("Error getting paginated reviews for music ID: {}", musicId, e);
            throw e;
        }
    }

    // Overloaded method to support pageable calls from controllers
    public Page<Review> getReviewsByMusic(Long musicId, Pageable pageable) {
        return getReviewsByMusicPaginated(musicId, pageable);
    }

    public Double getAverageRatingForMusic(Long musicId) {
        logger.debug("Getting average rating for music ID: {}", musicId);

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        try {
            List<Review> reviews = getReviewsByMusic(musicId);

            if (reviews.isEmpty()) {
                logger.info("No reviews found for music ID: {}, returning 0.0", musicId);
                return 0.0;
            }

            double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

            logger.info("Average rating for music ID {}: {}", musicId, averageRating);
            return averageRating;
        } catch (Exception e) {
            logger.error("Error getting average rating for music ID: {}", musicId, e);
            throw e;
        }
    }

    public List<Review> getReviewsByCustomer(Customer customer) {
        logger.debug("Getting reviews by customer: {}", customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        try {
            // Use findAll and filter manually since findByCustomer method may not exist
            List<Review> allReviews = reviewRepository.findAll();
            List<Review> reviews = allReviews.stream()
                .filter(review -> review.getCustomer().getId().equals(customer.getId()))
                .collect(java.util.stream.Collectors.toList());

            logger.info("Successfully retrieved {} reviews by customer: {}", reviews.size(), customer.getUsername());
            return reviews;
        } catch (Exception e) {
            logger.error("Error getting reviews by customer: {}", customer.getUsername(), e);
            throw e;
        }
    }

    public Optional<Review> findByMusicAndCustomer(Music music, Customer customer) {
        logger.debug("Finding review by music ID: {} and customer: {}",
                    music != null ? music.getId() : "null", customer != null ? customer.getUsername() : "null");

        if (music == null) {
            logger.error("Music is null");
            throw new ValidationException("Music cannot be null");
        }

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        try {
            // Use findAll and filter manually since findByMusicAndCustomer method may not exist
            List<Review> allReviews = reviewRepository.findAll();
            Optional<Review> review = allReviews.stream()
                .filter(r -> r.getMusic().getId().equals(music.getId()) &&
                           r.getCustomer().getId().equals(customer.getId()))
                .findFirst();

            if (review.isPresent()) {
                logger.info("Found review by music ID: {} and customer: {}", music.getId(), customer.getUsername());
            } else {
                logger.debug("No review found by music ID: {} and customer: {}", music.getId(), customer.getUsername());
            }
            return review;
        } catch (Exception e) {
            logger.error("Error finding review by music ID: {} and customer: {}", music.getId(), customer.getUsername(), e);
            throw new RuntimeException("Failed to find review", e);
        }
    }

    public Map<String, Object> getReviewStatistics() {
        logger.debug("Getting review statistics");

        try {
            Map<String, Object> stats = new HashMap<>();
            long totalReviews = reviewRepository.count();

            // Calculate overall average rating manually
            List<Review> allReviews = reviewRepository.findAll();
            double overallAverage = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

            stats.put("totalReviews", totalReviews);
            stats.put("averageRating", overallAverage);
            stats.put("reviewsThisMonth", 0); // Placeholder - would need date filtering

            logger.info("Successfully retrieved review statistics");
            return stats;
        } catch (Exception e) {
            logger.error("Error getting review statistics", e);
            throw new RuntimeException("Failed to get review statistics", e);
        }
    }

    public List<Review> getReviewsByMusicId(Long musicId) {
        logger.debug("Getting reviews by music ID: {}", musicId);
        return getReviewsByMusic(musicId);
    }

    public List<Review> getReviewsByUsername(String username) {
        logger.debug("Getting reviews by username: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }
        
        try {
            // This would need CustomerService integration to get Customer by username
            // For now, return empty list as placeholder
            logger.warn("getReviewsByUsername not fully implemented for username: {}", username);
            return List.of(); // Placeholder
        } catch (Exception e) {
            logger.error("Error getting reviews by username: {}", username, e);
            throw new RuntimeException("Failed to get reviews by username", e);
        }
    }

    public Optional<Review> getUserReviewForMusic(Long musicId, Customer customer) {
        logger.debug("Getting user review for music ID: {} by customer: {}", musicId, customer != null ? customer.getUsername() : "null");
        
        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }
        
        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }
        
        try {
            Music music = musicRepository.findById(musicId)
                    .orElseThrow(() -> {
                        logger.error("Music not found with ID: {}", musicId);
                        return new ResourceNotFoundException("Music", musicId.toString());
                    });
            
            return findByMusicAndCustomer(music, customer);
        } catch (Exception e) {
            logger.error("Error getting user review for music ID: {} by customer: {}", musicId, customer.getUsername(), e);
            throw e;
        }
    }

    public Map<String, Object> getReviewStats(Long musicId) {
        logger.debug("Getting review stats for music ID: {}", musicId);
        
        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }
        
        try {
            Map<String, Object> stats = new HashMap<>();
            List<Review> reviews = getReviewsByMusic(musicId);
            
            stats.put("totalReviews", reviews.size());
            stats.put("averageRating", getAverageRatingForMusic(musicId));
            
            // Rating distribution
            Map<Integer, Long> ratingDistribution = reviews.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Review::getRating,
                    java.util.stream.Collectors.counting()
                ));
            stats.put("ratingDistribution", ratingDistribution);
            
            logger.info("Successfully retrieved review stats for music ID: {}", musicId);
            return stats;
        } catch (Exception e) {
            logger.error("Error getting review stats for music ID: {}", musicId, e);
            throw e;
        }
    }

    public Map<String, Object> getArtistReviewsAnalytics(String artistUsername) {
        logger.debug("Getting artist reviews analytics for: {}", artistUsername);
        
        if (artistUsername == null || artistUsername.trim().isEmpty()) {
            logger.error("Artist username is null or empty");
            throw new ValidationException("Artist username cannot be null or empty");
        }
        
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            // This would need integration with MusicService to get artist's music
            // For now, return placeholder data
            analytics.put("totalReviews", 0);
            analytics.put("averageRating", 0.0);
            analytics.put("recentReviews", List.of());
            
            logger.warn("Artist reviews analytics not fully implemented for: {}", artistUsername);
            return analytics;
        } catch (Exception e) {
            logger.error("Error getting artist reviews analytics for: {}", artistUsername, e);
            throw new RuntimeException("Failed to get artist reviews analytics", e);
        }
    }

    /**
     * Updates the music entity's average rating and total reviews count.
     * This method is called automatically after any review create/update/delete operation.
     */
    private void updateMusicRatingStats(Music music) {
        logger.debug("Updating rating statistics for music ID: {}", music.getId());

        try {
            List<Review> reviews = getReviewsByMusic(music.getId());

            // Calculate new statistics
            int totalReviews = reviews.size();
            double averageRating = 0.0;

            if (totalReviews > 0) {
                averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
                // Round to 2 decimal places
                averageRating = Math.round(averageRating * 100.0) / 100.0;
            }

            // Update music entity
            music.setTotalReviews(totalReviews);
            music.setAverageRating(BigDecimal.valueOf(averageRating));
            musicRepository.save(music);

            logger.info("Updated rating stats for music ID {}: {} reviews, avg rating {}",
                       music.getId(), totalReviews, averageRating);

        } catch (Exception e) {
            logger.error("Error updating rating statistics for music ID: {}", music.getId(), e);
            // Don't throw - this shouldn't fail the main review operation
        }
    }

    // NEW: Admin review management methods
    public Page<Review> getAllReviewsForAdmin(int page, int size, String sortBy) {
        logger.debug("Getting all reviews for admin with page: {}, size: {}, sortBy: {}", page, size, sortBy);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "rating":
                    pageable = org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("rating").descending());
                    break;
                case "date":
                    pageable = org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending());
                    break;
                default:
                    pageable = org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending());
            }
        }

        return reviewRepository.findAll(pageable);
    }

    public void deleteReviewAsAdmin(Long reviewId) {
        logger.debug("Admin deleting review with ID: {}", reviewId);

        Optional<Review> reviewOptional = reviewRepository.findById(reviewId);
        if (reviewOptional.isEmpty()) {
            throw new ResourceNotFoundException("Review", reviewId.toString());
        }

        Review review = reviewOptional.get();
        Music music = review.getMusic();

        // Update music statistics
        updateMusicRatingAfterReviewDeletion(music, review.getRating());

        reviewRepository.deleteById(reviewId);
        logger.info("Admin successfully deleted review with ID: {}", reviewId);
    }

    public Page<Review> getFlaggedReviews(int page, int size) {
        logger.debug("Getting flagged reviews with page: {} and size: {}", page, size);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        // For now, return empty page as flagged reviews would need additional implementation
        return Page.empty();
    }

    // NEW: Analytics methods
    public long getTotalReviewsCount() {
        return reviewRepository.count();
    }

    public Map<String, Object> getReviewAnalytics(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();

        analytics.put("totalReviews", reviewRepository.count());
        analytics.put("averageRating", getAverageRating());
        analytics.put("ratingDistribution", getRatingDistribution());
        analytics.put("reviewsThisWeek", getReviewsCountInPeriod(java.time.LocalDate.now().minusWeeks(1), java.time.LocalDate.now()));
        analytics.put("reviewsThisMonth", getReviewsCountInPeriod(java.time.LocalDate.now().minusMonths(1), java.time.LocalDate.now()));

        if (startDate != null && endDate != null) {
            analytics.put("reviewsInPeriod", getReviewsCountInPeriod(startDate, endDate));
        }

        return analytics;
    }

    public Map<String, Long> getRatingDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i + " stars", reviewRepository.countByRating(i));
        }
        return distribution;
    }

    private double getAverageRating() {
        Double avgRating = reviewRepository.getAverageRating();
        return avgRating != null ? avgRating : 0.0;
    }

    private long getReviewsCountInPeriod(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        java.time.LocalDateTime startDateTime = startDate.atStartOfDay();
        java.time.LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return reviewRepository.countByCreatedAtBetween(startDateTime, endDateTime);
    }

    private void updateMusicRatingAfterReviewDeletion(Music music, Integer deletedRating) {
        // Recalculate music rating after review deletion
        List<Review> remainingReviews = reviewRepository.findByMusic(music);

        if (remainingReviews.isEmpty()) {
            music.setAverageRating(BigDecimal.ZERO);
            music.setTotalReviews(0);
        } else {
            double newAverage = remainingReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

            music.setAverageRating(BigDecimal.valueOf(newAverage));
            music.setTotalReviews(remainingReviews.size());
        }

        musicRepository.save(music);
    }
}
