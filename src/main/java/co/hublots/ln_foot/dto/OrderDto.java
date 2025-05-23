package co.hublots.ln_foot.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem;
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
    private LocalDateTime orderDate;

    @Builder.Default
    private String status = "pending";

    @Size(min = 1)
    @Valid
    private List<OrderItemDto> orderItems;

    private Double deliveryFee;
    private String deliveryAddress;
    private Double totalAmount;

    public static OrderDto fromEntity(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .deliveryFee(order.getDeliveryFee())
                .deliveryAddress(order.getDeliveryAddress())
                .totalAmount(order.getTotalAmount())
                .build();
    }

    public Order toEntity(String userId) {
        Order order = Order.builder()
                .id(id)
                .orderDate(LocalDateTime.now())
                .status(status)
                .userId(userId)
                .deliveryFee(deliveryFee)
                .deliveryAddress(deliveryAddress)
                .totalAmount(totalAmount) // totalAmount will be recalculated in service layer
                .build();

        if (orderItems != null) {
            List<OrderItem> entityOrderItems = orderItems.stream()
                    .map(itemDto -> itemDto.toEntity(order)) // Pass the order instance
                    .collect(Collectors.toList());
            order.setOrderItems(entityOrderItems);
        }
        return order;
    }
}