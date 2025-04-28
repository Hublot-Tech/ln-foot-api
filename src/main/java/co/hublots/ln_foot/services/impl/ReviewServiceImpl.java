package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Review;
import co.hublots.ln_foot.repositories.ProductRepository;
import co.hublots.ln_foot.repositories.ReviewRepository;
import co.hublots.ln_foot.services.ReviewService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private final ReviewRepository reviewRepository;
    
    @Autowired
    private final ProductRepository productRepository;

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public Review getReviewById(String id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Review not found with id: " + id));

    }

    @Override
    public Review createReview(Review review) {
        String productId = review.getProduct().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + productId));

        review.setProduct(product);
        return reviewRepository.save(review);
    }

    @Override
    public Review updateReview(String id, Review review) throws NoSuchElementException {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Review not found with id: " + id));

        Optional.of(review.getComment()).ifPresent(existingReview::setComment);
        Optional.of(review.getRating()).ifPresent(existingReview::setRating);
        return reviewRepository.save(existingReview);
    }

    @Override
    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }
}