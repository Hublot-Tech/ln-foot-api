package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Review;
import java.util.List;


public interface ReviewService {
    List<Review> getAllReviews();

    Review getReviewById(String id);

    Review createReview(Review reviewDto);

    Review updateReview(String id, Review reviewDto);

    void deleteReview(String id);
}