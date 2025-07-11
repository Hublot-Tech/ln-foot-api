package co.hublots.ln_foot.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import co.hublots.ln_foot.models.Category;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProductDto {
        private String id;

        private String imageUrl;

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Description is required")
        private String description;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        @Positive(message = "Stock quantity must be positive")
        // @NotBlank(message = "Stock quantity is required")
        private int stockQuantity;

        @NotBlank(message = "Category IDs are required")
        private List<String> categoryNames;

        @Builder.Default
        private List<String> sizes = List.of();

        public static ProductDto fromEntity(Product product) {
                return ProductDto.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .imageUrl(product.getImageUrl())
                                .stockQuantity(product.getStockQuantity())
                                .categoryNames(product.getCategories().stream().map(Category::getName)
                                                .collect(Collectors.toList()))
                                .sizes(product.getSizes().stream().map(Size::getName).collect(Collectors.toList()))
                                .build();
        }

        public Product toEntity() {
                return Product.builder()
                                .id(id)
                                .name(name)
                                .description(description)
                                .price(price)
                                .imageUrl(imageUrl)
                                .stockQuantity(stockQuantity)
                                .categories(categoryNames.stream()
                                                .map(category -> Category.builder().name(category).build())
                                                .collect(Collectors.toList()))
                                .sizes(sizes.stream().map(size -> Size.builder().name(size).build())
                                                .collect(Collectors.toList()))
                                .build();
        }

}