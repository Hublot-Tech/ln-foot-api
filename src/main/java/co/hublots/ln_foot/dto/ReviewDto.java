package co.hublots.ln_foot.dto;
import java.util.UUID;

import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReviewDto {
    private UUID id;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotNull(message = "Comment is required")
    @Size(max = 500, message = "Comment must be at most 500 characters")
    private String comment;

    public static ReviewDto fromEntity(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .build();
    }

    public Review toEntity() {
        return Review.builder()
                .id(id)
                .product(Product.builder().id(productId).build())
                .rating(rating)
                .comment(comment)
                .build();   
    }
}
