package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.ReviewDto;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Review;
import co.hublots.ln_foot.repositories.ProductRepository;
import co.hublots.ln_foot.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto reviewDto) {
        Product product = productRepository.findById(reviewDto.getProductId())
                .orElse(null);

        if (product == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Review review = reviewDto.toEntity(productRepository);

        Review savedReview = reviewRepository.save(review);
        return new ResponseEntity<>(ReviewDto.fromEntity(savedReview), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        if (!reviewRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        reviewRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}