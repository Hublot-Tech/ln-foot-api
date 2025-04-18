package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.annotations.KeycloakUserId;
import co.hublots.ln_foot.dto.ReviewDto;
import co.hublots.ln_foot.models.Review;
import co.hublots.ln_foot.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<Review> review = reviewService.getAllReviews();

        return new ResponseEntity<>(
                review.stream().map(ReviewDto::fromEntity).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable UUID id) {
        try {
            Review review = reviewService.getReviewById(id);
            return new ResponseEntity<>(ReviewDto.fromEntity(review), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<ReviewDto> createReview(
            @KeycloakUserId UUID userId,
            @Valid @RequestBody ReviewDto reviewDto) {

        Review review = reviewDto.toEntity();
        review.setKeycloakUserId(userId);
        try {
            Review savedReview = reviewService.createReview(review);
            return new ResponseEntity<>(
                    ReviewDto.fromEntity(savedReview),
                    HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable UUID id,
            @KeycloakUserId String userId,
            @RequestBody ReviewDto reviewDto) {

        try {
            Review review = reviewService.createReview(reviewDto.toEntity());
            return new ResponseEntity<>(ReviewDto.fromEntity(review), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
