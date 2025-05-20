package co.hublots.ln_foot.dto;

import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.models.OrderItem;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderItemDto {
    private String id;

    @NotBlank(message = "Product id is required")
    private String productVariantId;

    @NotBlank(message = "Quantity is required")
    private int quantity;

    private String size;

    public static OrderItemDto fromEntity(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .productVariantId(orderItem.getProductVariant().getId())
                .quantity(orderItem.getQuantity())
                .size(orderItem.getSize())
                .build();
    }

    public OrderItem toEntity() {
        return OrderItem.builder()
                .id(id)
                .productVariant(ProductVariant.builder().id(productVariantId).build())
                .quantity(quantity)
                .size(size)
                .build();
    }
}