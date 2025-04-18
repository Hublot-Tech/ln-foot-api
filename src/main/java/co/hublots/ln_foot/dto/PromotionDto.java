package co.hublots.ln_foot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;


import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Promotion;

@Data
@Builder
@AllArgsConstructor
public class PromotionDto {
    private String id;

    @NotNull(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Discounted price is required")
    @Positive(message = "Discounted price must be positive")
    private BigDecimal discountedPrice;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    public static PromotionDto fromEntity(Promotion promotion) {
        return PromotionDto.builder()
                .id(promotion.getId())
                .productId(promotion.getProduct().getId())
                .discountedPrice(promotion.getDiscountedPrice())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .build();
    }

    public Promotion toEntity() {
        return Promotion.builder()
                .id(id)
                .product(Product.builder().id(productId).build())
                .discountedPrice(discountedPrice)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
    
}       
