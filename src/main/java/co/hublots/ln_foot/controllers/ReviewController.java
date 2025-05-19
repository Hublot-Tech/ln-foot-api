package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.annotations.KeycloakUserId;
import co.hublots.ln_foot.dto.ReviewDto;
import co.hublots.ln_foot.models.Review;
import co.hublots.ln_foot.services.ReviewService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<Review> review = reviewService.getAllReviews();

        return new ResponseEntity<>(
                review.stream().map(ReviewDto::fromEntity).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable String id) {
        Review review = reviewService.getReviewById(id);
        return new ResponseEntity<>(ReviewDto.fromEntity(review), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto> createReview(
            @KeycloakUserId @Parameter(hidden = true) String userId,
            @RequestBody @Valid ReviewDto reviewDto) {
        Review review = reviewDto.toEntity();
        review.setKeycloakUserId(userId);

        Review savedReview = reviewService.createReview(review);
        return new ResponseEntity<>(
                ReviewDto.fromEntity(savedReview),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable String id,
            @KeycloakUserId @Parameter(hidden = true) String userId,
            @RequestBody ReviewDto reviewDto) {

        Review review = reviewService.createReview(reviewDto.toEntity());
        return new ResponseEntity<>(ReviewDto.fromEntity(review), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
