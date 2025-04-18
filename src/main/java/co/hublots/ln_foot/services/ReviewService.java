// src/main/java/co/hublots/ln_foot/services/ReviewService.java
package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Review;
import java.util.List;
import java.util.UUID;

public interface ReviewService {
    List<Review> getAllReviews();

    Review getReviewById(UUID id);

    Review createReview(Review reviewDto);

    Review updateReview(UUID id, Review reviewDto);

    void deleteReview(UUID id);
}