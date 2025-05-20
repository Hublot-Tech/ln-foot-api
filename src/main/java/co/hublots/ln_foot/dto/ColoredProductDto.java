package co.hublots.ln_foot.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import co.hublots.ln_foot.models.ColoredProduct;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ColoredProductDto {
    private String id;

    private String imageUrl;
    private MultipartFile file;

    @NotBlank(message = "Color name is required")
    private String colorCode;

    @NotBlank(message = "Product id is required")
    private String productId;

    @NotBlank(message = "Colored product is required")
    private double price;

    @NotBlank(message = "Stock quantity is required")
    @Positive(message = "Stock quantity must be positive")
    private int stockQuantity;

    @Builder.Default
    private List<String> sizes = List.of();

    public static ColoredProductDto fromEntity(ColoredProduct coloredProduct) {
        return ColoredProductDto.builder()
                .id(coloredProduct.getId())
                .colorCode(coloredProduct.getColorCode())
                .stockQuantity(coloredProduct.getStockQuantity())
                .price(coloredProduct.getPrice())
                .imageUrl(coloredProduct.getImageUrl())
                .sizes(coloredProduct.getSizes().stream().map(Size::getName).collect(Collectors.toList()))
                .productId(coloredProduct.getProduct().getId())
                .build();
    }

    public ColoredProduct toEntity() {
        return ColoredProduct.builder()
                .id(id)
                .colorCode(colorCode)
                .stockQuantity(stockQuantity)
                .price(price)
                .imageUrl(imageUrl)
                .sizes(sizes.stream().map(size -> Size.builder().name(size).build()).collect(Collectors.toList()))
                .product(Product.builder().id(productId).build())
                .build();
    }
}