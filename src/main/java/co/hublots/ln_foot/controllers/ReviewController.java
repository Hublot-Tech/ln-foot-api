package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Review;
import co.hublots.ln_foot.repositories.ProductRepository;
import co.hublots.ln_foot.repositories.ReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long id) {
        return reviewRepository.findById(id)
                .map(ReviewDto::fromEntity)
                .map(review -> new ResponseEntity<>(review, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<ReviewDto> createReview(@KeycloakUserId String userId, @Valid @RequestBody ReviewDto reviewDto) {
        Product product = productRepository.findById(reviewDto.getProductId())
                .orElse(null);

        if (product == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Review review = reviewDto.toEntity(productRepository);
        review.setKeycloakUserId(userId);
        Review savedReview = reviewRepository.save(review);
        return new ResponseEntity<>(ReviewDto.fromEntity(savedReview), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, @RequestBody ReviewDto reviewDto) {
        return reviewRepository.findById(id)
                .map(existingReview -> {
                    Product product = productRepository.findById(reviewDto.getProductId())
                            .orElse(null);

                    if (product == null) {
                        return new ResponseEntity<ReviewDto>(HttpStatus.BAD_REQUEST);
                    }

                    existingReview.setProduct(product);
                    existingReview.setRating(reviewDto.getRating());
                    existingReview.setComment(reviewDto.getComment());

                    Review updatedReview = reviewRepository.save(existingReview);
                    return new ResponseEntity<>(ReviewDto.fromEntity(updatedReview), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        if (!reviewRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        reviewRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}