package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.annotations.KeycloakUserId;
import co.hublots.ln_foot.dto.NotchPayDto;
import co.hublots.ln_foot.dto.OrderDto;
import co.hublots.ln_foot.dto.PaymentResponseDto;
import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.models.OrderItem;
import co.hublots.ln_foot.models.Payment;
import co.hublots.ln_foot.services.ProductVariantService;
import co.hublots.ln_foot.services.OrderService;
import co.hublots.ln_foot.services.PaymentService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ProductVariantService productVariantService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrders(@KeycloakUserId @Parameter(hidden = true) String userId) {
        List<Order> Orders = orderService.getAllOrders();
        return new ResponseEntity<>(Orders.stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping("/user/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDto>> getUserOrders(@KeycloakUserId @Parameter(hidden = true) String userId) {

        List<Order> Orders = orderService.getUserOrders(userId);
        return new ResponseEntity<>(Orders.stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String id) {
        Order Order = orderService.getOrderById(id);
        return new ResponseEntity<>(
                OrderDto.fromEntity(Order),
                HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> createOrder(
            @KeycloakUserId @Parameter(hidden = true) String userId,
            @Valid @RequestBody OrderDto orderDto) {
        Order order = orderDto.toEntity(userId);
        order.setOrderItems(orderDto.getOrderItems().stream()
                .map(item -> {
                    ProductVariant productVariant = productVariantService
                            .getProductVariantById(item.getProductVariantId());
                    if (productVariant == null) {
                        throw new IllegalArgumentException("Invalid product variant ID: " + item.getProductVariantId());
                    }
                    if (productVariant.getStockQuantity() < item.getQuantity()) {
                        throw new IllegalArgumentException(
                                "Not enough stock for product variant ID: " + item.getProductVariantId());
                    }
                    // Update the stock quantity
                    return OrderItem.builder()
                            .id(item.getId())
                            .productVariant(productVariant)
                            .price(productVariant.getPrice())
                            .quantity(item.getQuantity())
                            .size(item.getSize())
                            .build();
                })
                .collect(Collectors.toList()));

        orderService.createOrder(order);
        return new ResponseEntity<>(
                OrderDto.fromEntity(order),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> updateOrder(
            @KeycloakUserId @Parameter(hidden = true) String userId,
            @PathVariable String id,
            @Valid @RequestBody OrderDto orderDto) {
        Order order = orderDto.toEntity(userId);
        order.setOrderItems(orderDto.getOrderItems().stream()
                .map(item -> {
                    ProductVariant productVariant = productVariantService
                            .getProductVariantById(item.getProductVariantId());
                    if (productVariant == null) {
                        throw new IllegalArgumentException("Invalid product variant ID: " + item.getProductVariantId());
                    }
                    if (productVariant.getStockQuantity() < item.getQuantity()) {
                        throw new IllegalArgumentException(
                                "Not enough stock for product variant ID: " + item.getProductVariantId());
                    }
                    // Update the stock quantity
                    return OrderItem.builder()
                            .id(item.getId())
                            .productVariant(productVariant)
                            .price(productVariant.getPrice())
                            .quantity(item.getQuantity())
                            .size(item.getSize())
                            .build();
                })
                .collect(Collectors.toList()));

        Order updatedOrder = orderService.updateOrder(id, order);
        return new ResponseEntity<>(
                OrderDto.fromEntity(updatedOrder),
                HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponseDto> comfirmOrder(
            @PathVariable String id, @Valid @RequestBody NotchPayDto.InitiatePaymentRequest.Customer customer) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return new ResponseEntity<>(
                    HttpStatus.NOT_FOUND);
        }
        if (order.isCompleted()) {
            return new ResponseEntity<>(
                    HttpStatus.BAD_REQUEST);
        }
        // validate the order items (quantity, sizes)
        List<ProductVariant> productVariants = productVariantService
                .getProductVariantsByIds(order.getOrderItems().stream()
                        .map(item -> item.getProductVariant().getId())
                        .collect(Collectors.toList()));

        // Calculate the total amount of the order
        List<OrderItem> orderItems = order.getOrderItems();
        double amount = 0.0;
        for (var item : orderItems) {
            String productVariantId = item.getProductVariant().getId();
            ProductVariant productVariant = productVariants.stream()
                    .filter(cp -> cp.getId().equals(productVariantId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + item.getId()));

            if (productVariant.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product ID: " + productVariantId);
            }

            amount += item.getPrice() * item.getQuantity();
        }

        Payment payment = paymentService.confirmOrder(id, amount, customer.getEmail(), customer.getName(),
                customer.getPhone());

        return new ResponseEntity<>(PaymentResponseDto.fromEntity(payment), HttpStatus.ACCEPTED);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
