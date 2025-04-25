package co.hublots.ln_foot.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonAlias;

import co.hublots.ln_foot.models.Category;
import co.hublots.ln_foot.models.ColoredProduct;
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
        private MultipartFile file;

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Description is required")
        private String description;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        @JsonAlias({ "stock_quantity" })
        @NotBlank(message = "Stock quantity is required")
        @Positive(message = "Stock quantity must be positive")
        private int stockQuantity;

        @NotNull(message = "Category IDs are required")
        private List<String> categoryIds;

        @Builder.Default
        private List<String> sizeIds = List.of();

        @Builder.Default
        private List<String> coloredProductIds = List.of();

        public static ProductDto fromEntity(Product product) {
                return ProductDto.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .imageUrl(product.getImageUrl())
                                .stockQuantity(product.getStockQuantity())
                                .categoryIds(product.getCategories().stream().map(Category::getId)
                                                .collect(Collectors.toList()))
                                .sizeIds(product.getSizes().stream().map(Size::getId).collect(Collectors.toList()))
                                .coloredProductIds(product.getColoredProducts().stream().map(ColoredProduct::getId)
                                                .collect(Collectors.toList()))
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
                                .categories(categoryIds.stream()
                                                .map(id -> Category.builder().id(id).build())
                                                .collect(Collectors.toList()))
                                .sizes(sizeIds.stream()
                                                .map(id -> Size.builder().id(id).build())
                                                .collect(Collectors.toList()))
                                .coloredProducts(coloredProductIds.stream()
                                                .map(id -> ColoredProduct.builder().id(id).build())
                                                .collect(Collectors.toList()))
                                .build();
        }

}