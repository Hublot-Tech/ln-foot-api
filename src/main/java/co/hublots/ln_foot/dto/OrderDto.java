package co.hublots.ln_foot.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import co.hublots.ln_foot.models.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderDto {
    private String id;
    private String userId;
    private LocalDateTime orderDate;

    @Builder.Default
    private boolean isCompleted = false;

    @Size(min = 1)
    @Valid
    private List<OrderItemDto> orderItems;

    public static OrderDto fromEntity(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .isCompleted(order.isCompleted())
                .orderDate(order.getOrderDate())
                .userId(order.getUserId())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    public Order toEntity(String userId) {
        return Order.builder()
                .id(id)
                .orderDate(LocalDateTime.now())
                .orderItems(orderItems.stream()
                        .map(OrderItemDto::toEntity)
                        .collect(Collectors.toList()))
                .isCompleted(isCompleted)
                .userId(userId) // Set userId to null for now
                .build();
    }
}