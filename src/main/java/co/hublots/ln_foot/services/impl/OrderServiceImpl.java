package co.hublots.ln_foot.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode; // Added import
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem; // Added import
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
        // Set bidirectional relationship first
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                item.setOrder(order); // Essential for JPA relationships
            }
        }

        // Persist the order. With CascadeType.ALL, associated orderItems
        // (where item.setOrder(order) has been called) will also be persisted.
        Order savedOrder = orderRepository.save(order);

        // Calculate totalAmount using the helper method
        BigDecimal totalAmount = calculateOrderTotal(savedOrder.getOrderItems(), savedOrder.getDeliveryFee());
        savedOrder.setTotalAmount(totalAmount);
        
        // No second save needed here, JPA dirty checking handles persisting totalAmount
        // because 'savedOrder' is a managed entity.
        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrder(String id, Order orderDetails) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));

        // Yoda conditions for status check
        if ("completed".equals(existingOrder.getStatus()) || "cancelled".equals(existingOrder.getStatus())) {
            throw new InputMismatchException("Order with id " + id + " is " + existingOrder.getStatus() + " and cannot be updated.");
        }

        // Update basic fields
        existingOrder.setStatus(orderDetails.getStatus()); 
        existingOrder.setDeliveryAddress(orderDetails.getDeliveryAddress());
        existingOrder.setDeliveryFee(orderDetails.getDeliveryFee());

        // Handle OrderItems update using orphanRemoval=true strategy
        // The ProductVariant for orderDetails.getOrderItems() should be fetched and set by the caller (e.g., controller)
        // or this method needs access to ProductVariantRepository to fetch them.
        // Assuming orderDetails.getOrderItems() are fully formed entities with correct ProductVariant and price.
        if (existingOrder.getOrderItems() != null) {
            existingOrder.getOrderItems().clear(); // Clears existing items, orphanRemoval handles DB deletion
        } else {
            existingOrder.setOrderItems(new java.util.ArrayList<>()); // Ensure collection is initialized
        }
        
        if (orderDetails.getOrderItems() != null && !orderDetails.getOrderItems().isEmpty()) {
            for (OrderItem newItemFromDetails : orderDetails.getOrderItems()) {
                OrderItem item = OrderItem.builder()
                    .productVariant(newItemFromDetails.getProductVariant()) // Assuming this is already fetched
                    .quantity(newItemFromDetails.getQuantity())
                    .price(newItemFromDetails.getPrice()) // Assuming price is correct from DTO/controller
                    .size(newItemFromDetails.getSize())
                    .order(existingOrder) // Set the bidirectional relationship
                    .build();
                existingOrder.getOrderItems().add(item);
            }
        }
        // Explicit deleteAll and saveAll for items are removed due to CascadeType.ALL and orphanRemoval=true.
        
        // Calculate totalAmount using the helper method
        BigDecimal totalAmount = calculateOrderTotal(existingOrder.getOrderItems(), existingOrder.getDeliveryFee());
        existingOrder.setTotalAmount(totalAmount);

        return orderRepository.save(existingOrder); // Single save persists Order and cascades OrderItem changes
    }

    private BigDecimal calculateOrderTotal(List<OrderItem> orderItems, BigDecimal deliveryFee) {
        BigDecimal subTotal = BigDecimal.ZERO;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                if (item.getPrice() != null && item.getQuantity() > 0) {
                    BigDecimal itemPrice = item.getPrice().setScale(2, RoundingMode.HALF_UP);
                    BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
                                                    .setScale(2, RoundingMode.HALF_UP);
                    subTotal = subTotal.add(itemTotal);
                }
            }
        }
        subTotal = subTotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalDeliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        finalDeliveryFee = finalDeliveryFee.setScale(2, RoundingMode.HALF_UP);

        return subTotal.add(finalDeliveryFee).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

}