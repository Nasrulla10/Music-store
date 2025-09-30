package com.music.musicstore.api;

import com.music.musicstore.dto.CreateReviewRequest;
import com.music.musicstore.dto.ReviewDto;
import com.music.musicstore.models.music.Review;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewApiController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/music/{musicId}")
    public ResponseEntity<ReviewDto> createReview(@PathVariable Long musicId,
                                                @RequestBody CreateReviewRequest request,
                                                @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Review review = reviewService.createReview(musicId, customer, request.getRating(), request.getComment());
            return ResponseEntity.ok(convertToDto(review, customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long reviewId,
                                                @RequestBody CreateReviewRequest request,
                                                @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Review review = reviewService.updateReview(reviewId, customer, request.getRating(), request.getComment());
            return ResponseEntity.ok(convertToDto(review, customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                            @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            reviewService.deleteReview(reviewId, customer);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/music/{musicId}")
    public ResponseEntity<Page<ReviewDto>> getReviewsByMusic(@PathVariable Long musicId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @AuthenticationPrincipal Customer customer) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Review> reviews = reviewService.getReviewsByMusic(musicId, pageable);
            Page<ReviewDto> reviewDtos = reviews.map(review -> convertToDto(review, customer));
            return ResponseEntity.ok(reviewDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/music/{musicId}/my-review")
    public ResponseEntity<ReviewDto> getUserReview(@PathVariable Long musicId,
                                                 @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Optional<Review> review = reviewService.getUserReviewForMusic(musicId, customer);
            if (review.isPresent()) {
                return ResponseEntity.ok(convertToDto(review.get(), customer));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/music/{musicId}/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats(@PathVariable Long musicId) {
        try {
            Map<String, Object> stats = reviewService.getReviewStats(musicId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ReviewDto convertToDto(Review review, Customer currentUser) {
        boolean isOwnReview = currentUser != null &&
                             review.getCustomer().getId().equals(currentUser.getId());

        return new ReviewDto(
            review.getId(),
            review.getMusic().getId(),
            review.getCustomer().getUsername(),
            review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName(),
            review.getRating(),
            review.getComment(),
            review.getCreatedAt(),
            review.getUpdatedAt(),
            isOwnReview
        );
    }
}
