// src/main/java/co/hublots/ln_foot/dto/OrderDto.java
package co.hublots.ln_foot.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import co.hublots.ln_foot.models.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderDto {
    private UUID id;

    private LocalDate orderDate;

    private List<OrderItemDto> orderItems;

    public static OrderDto fromEntity(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    public Order toEntity() {
        return Order.builder()
                .id(id)
                .orderDate(orderDate)
                .build();
    }
}