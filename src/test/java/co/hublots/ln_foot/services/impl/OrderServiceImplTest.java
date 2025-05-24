package co.hublots.ln_foot.services.impl;

import java.math.BigDecimal; // Added import

import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem;
import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.repositories.OrderItemRepository;
import co.hublots.ln_foot.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue; // Added for BigDecimal comparison
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId("order123");
        order.setUserId("user1");
        order.setDeliveryAddress("123 Test St");
        order.setDeliveryFee(new BigDecimal("5.00")); // BigDecimal

        ProductVariant pv1 = new ProductVariant();
        pv1.setId("pv1");
        pv1.setPrice(new BigDecimal("10.00")); // BigDecimal

        ProductVariant pv2 = new ProductVariant();
        pv2.setId("pv2");
        pv2.setPrice(new BigDecimal("20.00")); // BigDecimal

        OrderItem item1 = OrderItem.builder()
                .id("item1")
                .productVariant(pv1)
                .price(new BigDecimal("10.00")) // BigDecimal
                .quantity(2) // 2 * 10 = 20
                .order(order)
                .build();

        OrderItem item2 = OrderItem.builder()
                .id("item2")
                .productVariant(pv2)
                .price(new BigDecimal("20.00")) // BigDecimal
                .quantity(1) // 1 * 20 = 20
                .order(order)
                .build();

        orderItems = new ArrayList<>(Arrays.asList(item1, item2));
        order.setOrderItems(orderItems);
    }

    @Test
    void createOrder_shouldCalculateTotalAmountAndSaveOrder() {
        // Arrange
        // Mocking the save operations
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        // orderItemRepository.saveAll is called within createOrder

        // Act
        Order createdOrder = orderService.createOrder(order);

        // Assert
        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderArgumentCaptor.capture());
        Order savedOrder = orderArgumentCaptor.getValue();

        // Expected total: (10.00 * 2) + (20.00 * 1) + 5.00 (deliveryFee) = 20 + 20 + 5
        // = 45.00
        assertTrue(new BigDecimal("45.00").compareTo(savedOrder.getTotalAmount()) == 0,
                "Total amount should be sum of items and delivery fee.");
        assertTrue(new BigDecimal("5.00").compareTo(savedOrder.getDeliveryFee()) == 0, "Delivery fee should be saved.");
        assertEquals("123 Test St", savedOrder.getDeliveryAddress(), "Delivery address should be saved.");
        assertEquals(order.getId(), createdOrder.getId(), "Returned order ID should match input.");
    }

    @Test
    void updateOrder_shouldRecalculateTotalAmountAndUpdateDetails() {
        // Arrange
        Order existingOrder = new Order();
        existingOrder.setId("order123");
        existingOrder.setUserId("user1");
        existingOrder.setStatus("pending");
        // Deep copy orderItems for existingOrder, ensuring their prices are BigDecimal
        List<OrderItem> existingItems = new ArrayList<>();
        for (OrderItem oi : orderItems) {
            existingItems.add(OrderItem.builder()
                    .id(oi.getId())
                    .productVariant(oi.getProductVariant()) // pv1 and pv2 already have BigDecimal prices
                    .price(oi.getPrice()) // price is already BigDecimal from setUp
                    .quantity(oi.getQuantity())
                    .order(existingOrder)
                    .build());
        }
        existingOrder.setOrderItems(existingItems);
        existingOrder.setDeliveryFee(new BigDecimal("5.00")); // BigDecimal
        // Calculate initial total for existingOrder: (10*2) + (20*1) + 5 = 45.00
        existingOrder.setTotalAmount(new BigDecimal("45.00")); // BigDecimal

        ProductVariant pv3 = new ProductVariant();
        pv3.setId("pv3");
        pv3.setPrice(new BigDecimal("15.00")); // BigDecimal

        OrderItem updatedItem = OrderItem.builder()
                .id("item3")
                .productVariant(pv3) // Different product variant
                .price(new BigDecimal("15.00")) // BigDecimal
                .quantity(1) // 1 * 15 = 15
                .order(existingOrder) // Will be associated with existingOrder
                .build();

        Order updatedDetails = new Order();
        updatedDetails.setOrderItems(new ArrayList<>(Arrays.asList(updatedItem)));
        updatedDetails.setDeliveryAddress("456 New Ave");
        updatedDetails.setDeliveryFee(new BigDecimal("10.00")); // New delivery fee - BigDecimal
        updatedDetails.setStatus("pending"); // Status can be part of update

        when(orderRepository.findById("order123")).thenReturn(java.util.Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Mocking orderItemRepository.deleteAll and saveAll as they are called in the
        // service

        // Act
        Order resultOrder = orderService.updateOrder("order123", updatedDetails);

        // Assert
        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderArgumentCaptor.capture());
        Order savedOrder = orderArgumentCaptor.getValue();

        // Expected total for updated order: (15.00 * 1) + 10.00 (new deliveryFee) = 15
        // + 10 = 25.00
        assertTrue(new BigDecimal("25.00").compareTo(savedOrder.getTotalAmount()) == 0,
                "Total amount should be recalculated.");
        assertTrue(new BigDecimal("10.00").compareTo(savedOrder.getDeliveryFee()) == 0,
                "Delivery fee should be updated.");
        assertEquals("456 New Ave", savedOrder.getDeliveryAddress(), "Delivery address should be updated.");
        assertEquals("order123", resultOrder.getId());
        assertEquals(1, savedOrder.getOrderItems().size(), "Order items should be updated.");
        assertEquals("pv3", savedOrder.getOrderItems().get(0).getProductVariant().getId(),
                "Updated product variant ID should match.");
    }
}
