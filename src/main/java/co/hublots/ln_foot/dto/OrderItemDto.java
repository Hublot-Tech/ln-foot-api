package co.hublots.ln_foot.dto;

import java.math.BigDecimal; // Added import

import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem;
import co.hublots.ln_foot.models.ProductVariant;
import jakarta.validation.constraints.Min; // Added import
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

    @Min(value = 1, message = "Quantity must be at least 1") // Replaced @NotBlank
    private int quantity;

    private String size;
    private String orderId;
    private BigDecimal price; // Changed to BigDecimal

    public static OrderItemDto fromEntity(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .productVariantId(orderItem.getProductVariant().getId())
                .quantity(orderItem.getQuantity())
                .orderId(orderItem.getOrder().getId())
                .price(orderItem.getPrice()) // Assuming OrderItem entity is updated
                .size(orderItem.getSize())
                .build();
    }

    public OrderItem toEntity(Order order) {
        return OrderItem.builder()
                .id(id)
                .order(order)
                .productVariant(ProductVariant.builder().id(productVariantId).build())
                .quantity(quantity)
                .price(price) // This will be BigDecimal
                .size(size)
                .build();
    }
}