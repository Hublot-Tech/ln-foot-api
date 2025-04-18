package co.hublots.ln_foot.services;

import java.util.List;
import java.util.UUID;

import co.hublots.ln_foot.models.Order;

public interface OrderService {
    List<Order> getAllOrders();

    List<Order> getUserOrders(UUID userId);

    Order getOrderById(UUID id);

    Order createOrder(Order order);

    Order updateOrder(UUID id, Order order);

    void deleteOrder(UUID id);
}
