package co.hublots.ln_foot.dto;

import java.math.BigDecimal; // Added import
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero; // Added import
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

    @PositiveOrZero(message = "Delivery fee must be zero or positive") // Added validation
    private BigDecimal deliveryFee; // Changed to BigDecimal
    private String deliveryAddress;
    private BigDecimal totalAmount; // Changed to BigDecimal

    public static OrderDto fromEntity(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .orderItems(order.getOrderItems() == null ? java.util.Collections.emptyList() : 
                             order.getOrderItems().stream()
                                  .map(OrderItemDto::fromEntity)
                                  .collect(Collectors.toList())) // Handled null orderItems
                .deliveryFee(order.getDeliveryFee()) // Assuming Order entity is updated
                .deliveryAddress(order.getDeliveryAddress())
                .totalAmount(order.getTotalAmount()) // Assuming Order entity is updated
                .build();
    }

    public Order toEntity(String userId) {
        Order order = Order.builder()
                // .id(id) // Removed .id(id)
                .orderDate(orderDate != null ? orderDate : LocalDateTime.now())
                .status(status)
                .userId(userId)
                .deliveryFee(deliveryFee) // This will be BigDecimal
                .deliveryAddress(deliveryAddress)
                // .totalAmount(totalAmount) // totalAmount is recalculated in service layer
                .build();

        if (orderItems != null) {
            List<OrderItem> entityOrderItems = orderItems.stream()
                    .map(itemDto -> itemDto.toEntity(order)) // Pass the order instance
                    .collect(Collectors.toList());
            order.setOrderItems(entityOrderItems);
        } else {
            order.setOrderItems(java.util.Collections.emptyList()); // Use java.util.Collections
        }
        return order;
    }
}