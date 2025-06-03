package co.hublots.ln_foot.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem;
import co.hublots.ln_foot.repositories.OrderRepository;
import co.hublots.ln_foot.services.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        order.setStatus("pending");
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                item.setOrder(order);
            }
        }

        Order savedOrder = orderRepository.save(order);
        BigDecimal totalAmount = calculateOrderTotal(savedOrder.getOrderItems(), savedOrder.getDeliveryFee());
        savedOrder.setTotalAmount(totalAmount);
        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrder(String id, Order orderDetails) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));

        if ("completed".equals(existingOrder.getStatus()) || "cancelled".equals(existingOrder.getStatus())) {
            throw new InputMismatchException(
                    "Order with id " + id + " is " + existingOrder.getStatus() + " and cannot be updated.");
        }

        existingOrder.setStatus(orderDetails.getStatus());
        existingOrder.setDeliveryAddress(orderDetails.getDeliveryAddress());
        existingOrder.setDeliveryFee(orderDetails.getDeliveryFee());

        if (existingOrder.getOrderItems() != null) {
            existingOrder.getOrderItems().clear();
        } else {
            existingOrder.setOrderItems(new java.util.ArrayList<>());
        }

        if (orderDetails.getOrderItems() != null && !orderDetails.getOrderItems().isEmpty()) {
            for (OrderItem newItemFromDetails : orderDetails.getOrderItems()) {
                OrderItem item = OrderItem.builder()
                        .productVariant(newItemFromDetails.getProductVariant())
                        .quantity(newItemFromDetails.getQuantity())
                        .price(newItemFromDetails.getPrice())
                        .size(newItemFromDetails.getSize())
                        .order(existingOrder)
                        .build();
                existingOrder.getOrderItems().add(item);
            }
        }

        BigDecimal totalAmount = calculateOrderTotal(existingOrder.getOrderItems(), existingOrder.getDeliveryFee());
        existingOrder.setTotalAmount(totalAmount);

        return orderRepository.save(existingOrder);
    }

    private BigDecimal calculateOrderTotal(List<OrderItem> orderItems, BigDecimal deliveryFee) {
        BigDecimal subTotal = BigDecimal.ZERO;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                if (item.getPrice() != null && item.getQuantity() > 0) {
                    BigDecimal itemPrice = roundToTwoDecimalPlaces(item.getPrice());
                    BigDecimal itemTotal = roundToTwoDecimalPlaces(
                            itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                    subTotal = subTotal.add(itemTotal);
                }
            }
        }

        BigDecimal finalDeliveryFee = deliveryFee != null ? roundToTwoDecimalPlaces(deliveryFee) : BigDecimal.ZERO;
        return roundToTwoDecimalPlaces(subTotal.add(finalDeliveryFee));
    }

    private BigDecimal roundToTwoDecimalPlaces(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }
}
