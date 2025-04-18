package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.InputMismatchException;
import java.util.Optional;


import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.repositories.OrderRepository;
import co.hublots.ln_foot.services.OrderService;
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
        Order orderExample = Order.builder()
                .userId(userId) // Assuming Order has a userId field
                .build();

        Example<Order> example = Example.of(orderExample);

        return orderRepository.findAll(example);
    }

    @Override
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));
    }

    @Override
    public Order createOrder(Order order) {
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