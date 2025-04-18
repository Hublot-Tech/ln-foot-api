// src/main/java/co/hublots/ln_foot/dto/OrderItemDto.java
package co.hublots.ln_foot.dto;

import java.math.BigDecimal;
import java.util.UUID;

import co.hublots.ln_foot.models.Color;
import co.hublots.ln_foot.models.OrderItem;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderItemDto {
    private UUID id;

    private UUID productId;

    private int quantity;

    private UUID sizeId;

    private UUID colorId;

    private BigDecimal price;

    public static OrderItemDto fromEntity(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .quantity(orderItem.getQuantity())
                .sizeId(orderItem.getSize().getId())
                .colorId(orderItem.getColor().getId())
                .price(orderItem.getPrice())
                .build();
    }

    public OrderItem toEntity() {
        return OrderItem.builder()
                .id(id)
                .product(Product.builder().id(productId).build())
                .quantity(quantity)
                .size(Size.builder().id(sizeId).build())
                .color(Color.builder().id(colorId).build())
                .price(price)
                .build();
    }
}