package co.hublots.ln_foot.dto;

import java.math.BigDecimal; // Added import
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Size;
import jakarta.validation.constraints.DecimalMin; // Added import
import jakarta.validation.constraints.Digits; // Added import
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Added import
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProductVariantDto {
    private String id;

    private String imageUrl;
    private MultipartFile file;

    @NotBlank(message = "Color name is required")
    private String colorCode;

    @NotBlank(message = "Product id is required")
    private String productId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Price has invalid format")
    private BigDecimal price; // Changed to BigDecimal

    @Positive(message = "Stock quantity must be positive") // Comment removed
    private int stockQuantity;

    @Builder.Default
    private List<String> sizes = List.of();

    public static ProductVariantDto fromEntity(ProductVariant productVariant) {
        return ProductVariantDto.builder()
                .id(productVariant.getId())
                .colorCode(productVariant.getColorCode())
                .stockQuantity(productVariant.getStockQuantity())
                .price(productVariant.getPrice()) // Assuming ProductVariant entity is updated
                .imageUrl(productVariant.getImageUrl())
                .sizes(productVariant.getSizes().stream().map(Size::getName).collect(Collectors.toList()))
                .productId(productVariant.getProduct().getId())
                .build();
    }

    public ProductVariant toEntity() {
        return ProductVariant.builder()
                .id(id)
                .colorCode(colorCode)
                .stockQuantity(stockQuantity)
                .price(price) // This will be BigDecimal
                .imageUrl(imageUrl)
                .sizes(sizes.stream().map(size -> Size.builder().name(size).build()).collect(Collectors.toList()))
                .product(Product.builder().id(productId).build())
                .build();
    }
}