package co.hublots.ln_foot.dto;

import org.springframework.web.multipart.MultipartFile;

import co.hublots.ln_foot.models.ColoredProduct;
import jakarta.validation.constraints.NotBlank;
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
    private String name;

    @NotBlank(message = "Product id is required")
    private String productId;

    @NotBlank(message = "Colored product is required")
    private double price;

    public static ColoredProductDto fromEntity(ColoredProduct coloredProduct) {
        return ColoredProductDto.builder()
                .id(coloredProduct.getId())
                .name(coloredProduct.getName())
                .productId(coloredProduct.getProduct().getId())
                .build();
    }

    public ColoredProduct toEntity() {
        return ColoredProduct.builder()
                .id(id)
                .name(name)
                .build();
    }
}