package co.hublots.ln_foot.services.impl;

import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.repositories.OrderItemRepository;
import co.hublots.ln_foot.repositories.OrderRepository;
import co.hublots.ln_foot.services.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findAllByUserId(userId);
    }

    @Override
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // Ensure order items are linked to the order if not already
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                item.setOrder(order); // Ensure bidirectional relationship is set
                // Assuming productVariant details (like price) are fetched and set earlier,
                // or OrderItem.price is pre-populated from DTO.
            }
            // Persist order items - this might be handled by cascade if configured on Order.
            // Explicitly saving them ensures they have IDs before calculating total.
            orderItemRepository.saveAll(order.getOrderItems());
        }

        double subTotal = 0.0;
        if (order.getOrderItems() != null) {
            subTotal = order.getOrderItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }

        double deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : 0.0;
        order.setTotalAmount(subTotal + deliveryFee);

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrder(String id, Order orderDetails) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));

        if (existingOrder.getStatus().equals("completed") || existingOrder.getStatus().equals("cancelled")) {
            throw new InputMismatchException("Order with id " + id + " is " + existingOrder.getStatus() + " and cannot be updated.");
        }

        // Update basic fields
        existingOrder.setStatus(orderDetails.getStatus()); // Allow status updates
        existingOrder.setDeliveryAddress(orderDetails.getDeliveryAddress());
        existingOrder.setDeliveryFee(orderDetails.getDeliveryFee());

        // Handle OrderItems update
        if (orderDetails.getOrderItems() != null && !orderDetails.getOrderItems().isEmpty()) {
            // Clear existing items and add new ones (simple approach)
            // For more complex scenarios (e.g., keeping existing items not in new list),
            // more sophisticated merging logic would be needed.
            existingOrder.getOrderItems().clear();
            orderItemRepository.deleteAll(orderRepository.findById(id).get().getOrderItems()); // remove old items


            for (OrderItem newItemDto : orderDetails.getOrderItems()) {
                OrderItem item = OrderItem.builder()
                .productVariant(newItemDto.getProductVariant())
                .quantity(newItemDto.getQuantity())
                .price(newItemDto.getPrice()) // Ensure price is carried over
                .size(newItemDto.getSize())
                .order(existingOrder)
                .build();
                existingOrder.getOrderItems().add(item);
            }
            orderItemRepository.saveAll(existingOrder.getOrderItems());
        }


        double subTotal = 0.0;
        if (existingOrder.getOrderItems() != null) {
            subTotal = existingOrder.getOrderItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }

        double deliveryFee = existingOrder.getDeliveryFee() != null ? existingOrder.getDeliveryFee() : 0.0;
        existingOrder.setTotalAmount(subTotal + deliveryFee);

        return orderRepository.save(existingOrder);
    }

    @Override
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

}