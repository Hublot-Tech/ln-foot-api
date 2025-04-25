package co.hublots.ln_foot.dto;

import co.hublots.ln_foot.models.ColoredProduct;
import co.hublots.ln_foot.models.OrderItem;
import co.hublots.ln_foot.models.Size;
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
    private String coloredProductId;

    @NotBlank(message = "Quantity is required")
    private int quantity;

    private String sizeId;

    public static OrderItemDto fromEntity(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .coloredProductId(orderItem.getColoredProduct().getId())
                .quantity(orderItem.getQuantity())
                .sizeId(orderItem.getSize().getId())
                .build();
    }

    public OrderItem toEntity() {
        return OrderItem.builder()
                .id(id)
                .coloredProduct(ColoredProduct.builder().id(coloredProductId).build())
                .quantity(quantity)
                .size(Size.builder().id(sizeId).build())
                .build();
    }
}