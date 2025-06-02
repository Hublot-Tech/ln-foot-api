package co.hublots.ln_foot.services;

import java.util.List;
import java.util.Optional;

import co.hublots.ln_foot.models.Order;

public interface OrderService {
    List<Order> getAllOrders();

    List<Order> getUserOrders(String userId);

    Optional<Order> getOrderById(String id);

    Order createOrder(Order order);

    Order updateOrder(String id, Order order);

    void deleteOrder(String id);
}
