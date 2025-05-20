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
    public Order createOrder(Order order) {
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            orderItemRepository.saveAll(order.getOrderItems());
        }

        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(String id, Order order) {
        Order existingCategory = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));

        if (existingCategory.isCompleted()) {
            throw new InputMismatchException("Order with id" + id + "cannot be updated");
        }

        Optional.of(order.getOrderItems()).ifPresent(existingCategory::setOrderItems);

        return orderRepository.save(existingCategory);
    }

    @Override
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

}