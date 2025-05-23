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

        // Persist the order to obtain its ID (if generated) and manage its lifecycle.
        // With CascadeType.ALL, associated orderItems (if order field is set on them) will also be persisted.
        Order savedOrder = orderRepository.save(order);
        // Explicit save of order items is removed due to CascadeType.ALL
        // if (savedOrder.getOrderItems() != null && !savedOrder.getOrderItems().isEmpty()) {
        //     orderItemRepository.saveAll(savedOrder.getOrderItems());
        // }

        // Calculate totalAmount with proper scaling
        BigDecimal subTotal = BigDecimal.ZERO;
        if (savedOrder.getOrderItems() != null) {
            for (OrderItem item : savedOrder.getOrderItems()) {
                if (item.getPrice() != null && item.getQuantity() > 0) {
                    BigDecimal itemPrice = item.getPrice().setScale(2, RoundingMode.HALF_UP);
                    BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
                                                    .setScale(2, RoundingMode.HALF_UP);
                    subTotal = subTotal.add(itemTotal);
                }
            }
        }
        subTotal = subTotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal deliveryFee = savedOrder.getDeliveryFee() != null ? savedOrder.getDeliveryFee() : BigDecimal.ZERO;
        deliveryFee = deliveryFee.setScale(2, RoundingMode.HALF_UP);
        
        savedOrder.setTotalAmount(subTotal.add(deliveryFee).setScale(2, RoundingMode.HALF_UP));
        
        // Second save is removed; JPA dirty checking handles persisting totalAmount.
        return savedOrder;
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
        if (orderDetails.getOrderItems() != null) { // Check orderDetails for items
            // Clear existing items from the order and database
            if (existingOrder.getOrderItems() != null && !existingOrder.getOrderItems().isEmpty()) {
                orderItemRepository.deleteAll(existingOrder.getOrderItems());
                existingOrder.getOrderItems().clear(); // Clear the collection in the entity
            }

            // Add new items from orderDetails
            if (!orderDetails.getOrderItems().isEmpty()) {
                for (OrderItem newItemFromDetails : orderDetails.getOrderItems()) {
                    OrderItem item = OrderItem.builder()
                        .productVariant(newItemFromDetails.getProductVariant())
                        .quantity(newItemFromDetails.getQuantity())
                        .price(newItemFromDetails.getPrice()) // Ensure price is carried over
                        .size(newItemFromDetails.getSize())
                        .order(existingOrder) // Set the bidirectional relationship
                        .build();
                    existingOrder.getOrderItems().add(item);
                }
                orderItemRepository.saveAll(existingOrder.getOrderItems());
            }
        } else { // if orderDetails.getOrderItems() is null, implies removing all items
            if (existingOrder.getOrderItems() != null && !existingOrder.getOrderItems().isEmpty()) {
                orderItemRepository.deleteAll(existingOrder.getOrderItems());
                existingOrder.getOrderItems().clear();
            }
        }

        // Recalculate totalAmount
        BigDecimal subTotal = BigDecimal.ZERO;
        if (existingOrder.getOrderItems() != null) {
             for (OrderItem item : existingOrder.getOrderItems()) {
                if (item.getPrice() != null && item.getQuantity() > 0) {
                    BigDecimal itemPrice = item.getPrice().setScale(2, RoundingMode.HALF_UP);
                    BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
                                                    .setScale(2, RoundingMode.HALF_UP);
                    subTotal = subTotal.add(itemTotal);
                }
            }
        }
        subTotal = subTotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal deliveryFee = existingOrder.getDeliveryFee() != null ? existingOrder.getDeliveryFee() : BigDecimal.ZERO;
        deliveryFee = deliveryFee.setScale(2, RoundingMode.HALF_UP);
        
        existingOrder.setTotalAmount(subTotal.add(deliveryFee).setScale(2, RoundingMode.HALF_UP));

        return orderRepository.save(existingOrder);
    }

    @Override
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

}