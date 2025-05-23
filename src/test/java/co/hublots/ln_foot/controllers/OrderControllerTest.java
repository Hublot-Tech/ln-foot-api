package co.hublots.ln_foot.controllers;

import java.math.BigDecimal;

import co.hublots.ln_foot.dto.NotchPayDto;
import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem;
import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.models.Payment;
import co.hublots.ln_foot.services.OrderService;
import co.hublots.ln_foot.services.PaymentService;
import co.hublots.ln_foot.services.ProductVariantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList; 
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ProductVariantService productVariantService; // Added as it's used in confirmOrder

    @InjectMocks
    private OrderController orderController;

    private Order sampleOrder;
    private NotchPayDto.InitiatePaymentRequest.Customer customerDto;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId("orderTest123");
        sampleOrder.setStatus("pending");
        sampleOrder.setDeliveryFee(new BigDecimal("10.00")); // BigDecimal

        ProductVariant pv1 = new ProductVariant();
        pv1.setId("pv1");
        pv1.setPrice(new BigDecimal("25.00")); // BigDecimal
        pv1.setStockQuantity(10);

        OrderItem item1 = OrderItem.builder()
                .id("item1")
                .productVariant(pv1)
                .price(new BigDecimal("25.00")) // BigDecimal
                .quantity(2)  // 2 * 25 = 50
                .order(sampleOrder)
                .build();
        
        List<OrderItem> orderItems = new ArrayList<>(Arrays.asList(item1));
        sampleOrder.setOrderItems(orderItems);

        // Total amount: (25.00 * 2) + 10.00 (deliveryFee) = 50 + 10 = 60.00
        sampleOrder.setTotalAmount(new BigDecimal("60.00")); // BigDecimal

        // Corrected Customer DTO instantiation order
        customerDto = new NotchPayDto.InitiatePaymentRequest.Customer("Test User", "test@example.com", "1234567890");
    }

    @Test
    void confirmOrder_shouldUseTotalAmountFromOrderEntity() {
        // Arrange
        when(orderService.getOrderById("orderTest123")).thenReturn(sampleOrder);

        // Mock productVariantService to return the product variants when asked
        List<ProductVariant> productVariantsInOrder = sampleOrder.getOrderItems().stream()
            .map(OrderItem::getProductVariant)
            .collect(Collectors.toList());
        when(productVariantService.getProductVariantsByIds(anyList())).thenReturn(productVariantsInOrder); // Used anyList()
        
        Payment mockPayment = new Payment(); // Mock payment object
        mockPayment.setId("payment123");
        mockPayment.setStatus("pending");
        when(paymentService.confirmOrder(anyString(), anyDouble(), anyString(), anyString(), anyString()))
            .thenReturn(mockPayment);

        // Act
        ResponseEntity<?> response = orderController.confirmOrder("orderTest123", customerDto); // Method call updated

        // Assert
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(paymentService).confirmOrder(
            anyString(), 
            amountCaptor.capture(), 
            anyString(), 
            anyString(), 
            anyString()
        );

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        
        // Use BigDecimal for comparing the expected amount with the captured double value
        BigDecimal expectedAmount = sampleOrder.getTotalAmount(); // This is already BigDecimal("60.00")
        assertEquals(0, BigDecimal.valueOf(amountCaptor.getValue()).compareTo(expectedAmount),
            "Amount passed to paymentService should be numerically equal to Order.totalAmount (after Order.totalAmount is converted to double for the call).");
    }
}
