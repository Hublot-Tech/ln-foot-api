package co.hublots.ln_foot.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.hublots.ln_foot.dto.NotchPayDto;
import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem;
import co.hublots.ln_foot.models.Payment;
import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.models.User.Customer;
import co.hublots.ln_foot.services.OrderService;
import co.hublots.ln_foot.services.PaymentService;
import co.hublots.ln_foot.services.ProductVariantService;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ProductVariantService productVariantService;

    @InjectMocks
    private OrderController orderController;

    private Order sampleOrder;
    private NotchPayDto.InitiatePaymentRequest.Customer customerDto;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId("orderTest123");
        sampleOrder.setStatus("pending");
        sampleOrder.setDeliveryFee(new BigDecimal("10.00"));

        ProductVariant pv1 = new ProductVariant();
        pv1.setId("pv1");
        pv1.setPrice(new BigDecimal("25.00"));
        pv1.setStockQuantity(10);

        OrderItem item1 = OrderItem.builder()
                .id("item1")
                .productVariant(pv1)
                .price(new BigDecimal("25.00"))
                .quantity(2)
                .order(sampleOrder)
                .build();

        sampleOrder.setOrderItems(new ArrayList<>(Arrays.asList(item1)));
        sampleOrder.setTotalAmount(new BigDecimal("60.00"));

        customerDto = new NotchPayDto.InitiatePaymentRequest.Customer("Test User", "test@example.com", "1234567890");
    }

    @Test
    void confirmOrder_shouldUseTotalAmountFromOrderEntity() {
        when(orderService.getOrderById("orderTest123")).thenReturn(Optional.of(sampleOrder));

        List<ProductVariant> productVariantsInOrder = sampleOrder.getOrderItems().stream()
                .map(OrderItem::getProductVariant)
                .collect(Collectors.toList());
        when(productVariantService.getProductVariantsByIds(anyList())).thenReturn(productVariantsInOrder);

        Payment mockPayment = Payment.builder()
                .id("payment123")
                .orderId(sampleOrder.getId())
                .paymentRef("ref123")
                .status("pending")
                .build();
        when(paymentService.initiateHostedPayment(anyString(), anyDouble(), any(Customer.class), anyString()))
                .thenReturn(mockPayment);

        ResponseEntity<?> response = orderController.finalyzeOrder("orderTest123", "com.lnfoot://", customerDto);

        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(paymentService)
                .initiateHostedPayment(anyString(), amountCaptor.capture(), any(Customer.class), anyString());

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        BigDecimal expectedAmount = sampleOrder.getTotalAmount();
        assertEquals(0, BigDecimal.valueOf(amountCaptor.getValue()).compareTo(expectedAmount));
    }
}
