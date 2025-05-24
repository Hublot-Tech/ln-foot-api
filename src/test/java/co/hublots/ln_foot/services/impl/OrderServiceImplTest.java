package co.hublots.ln_foot.services.impl;

import java.math.BigDecimal;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        order.setDeliveryFee(new BigDecimal("5.00"));

        ProductVariant pv1 = new ProductVariant();
        pv1.setId("pv1");
        pv1.setPrice(new BigDecimal("10.00"));

        ProductVariant pv2 = new ProductVariant();
        pv2.setId("pv2");
        pv2.setPrice(new BigDecimal("20.00"));

        OrderItem item1 = OrderItem.builder()
                .id("item1")
                .productVariant(pv1)
                .price(new BigDecimal("10.00"))
                .quantity(2)
                .order(order)
                .build();

        OrderItem item2 = OrderItem.builder()
                .id("item2")
                .productVariant(pv2)
                .price(new BigDecimal("20.00"))
                .quantity(1)
                .order(order)
                .build();

        orderItems = new ArrayList<>(Arrays.asList(item1, item2));
        order.setOrderItems(orderItems);
    }

    @Test
    void createOrder_shouldCalculateTotalAmountAndSaveOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order createdOrder = orderService.createOrder(order);

        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderArgumentCaptor.capture());
        Order savedOrder = orderArgumentCaptor.getValue();

        assertTrue(new BigDecimal("45.00").compareTo(savedOrder.getTotalAmount()) == 0);
        assertTrue(new BigDecimal("5.00").compareTo(savedOrder.getDeliveryFee()) == 0);
        assertEquals("123 Test St", savedOrder.getDeliveryAddress());
        assertEquals(order.getId(), createdOrder.getId());
    }

    @Test
    void updateOrder_shouldRecalculateTotalAmountAndUpdateDetails() {
        Order existingOrder = new Order();
        existingOrder.setId("order123");
        existingOrder.setUserId("user1");
        existingOrder.setStatus("pending");

        List<OrderItem> existingItems = new ArrayList<>();
        for (OrderItem oi : orderItems) {
            existingItems.add(OrderItem.builder()
                    .id(oi.getId())
                    .productVariant(oi.getProductVariant())
                    .price(oi.getPrice())
                    .quantity(oi.getQuantity())
                    .order(existingOrder)
                    .build());
        }
        existingOrder.setOrderItems(existingItems);
        existingOrder.setDeliveryFee(new BigDecimal("5.00"));
        existingOrder.setTotalAmount(new BigDecimal("45.00"));

        ProductVariant pv3 = new ProductVariant();
        pv3.setId("pv3");
        pv3.setPrice(new BigDecimal("15.00"));

        OrderItem updatedItem = OrderItem.builder()
                .id("item3")
                .productVariant(pv3)
                .price(new BigDecimal("15.00"))
                .quantity(1)
                .order(existingOrder)
                .build();

        Order updatedDetails = new Order();
        updatedDetails.setOrderItems(new ArrayList<>(Arrays.asList(updatedItem)));
        updatedDetails.setDeliveryAddress("456 New Ave");
        updatedDetails.setDeliveryFee(new BigDecimal("10.00"));
        updatedDetails.setStatus("pending");

        when(orderRepository.findById("order123")).thenReturn(java.util.Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order resultOrder = orderService.updateOrder("order123", updatedDetails);

        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderArgumentCaptor.capture());
        Order savedOrder = orderArgumentCaptor.getValue();

        assertTrue(new BigDecimal("25.00").compareTo(savedOrder.getTotalAmount()) == 0);
        assertTrue(new BigDecimal("10.00").compareTo(savedOrder.getDeliveryFee()) == 0);
        assertEquals("456 New Ave", savedOrder.getDeliveryAddress());
        assertEquals("order123", resultOrder.getId());
        assertEquals(1, savedOrder.getOrderItems().size());
        assertEquals("pv3", savedOrder.getOrderItems().get(0).getProductVariant().getId());
    }
}
