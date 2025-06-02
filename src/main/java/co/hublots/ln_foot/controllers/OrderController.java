package co.hublots.ln_foot.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(
                orders.stream().map(OrderDto::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/user/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDto>> getUserOrders(@KeycloakUserId @Parameter(hidden = true) String userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(
                orders.stream().map(OrderDto::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String id) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));
        return ResponseEntity.ok(OrderDto.fromEntity(order));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> createOrder(
            @KeycloakUserId @Parameter(hidden = true) String userId,
            @Valid @RequestBody OrderDto orderDto) {

        Order order = orderDto.toEntity(userId);
        order.setOrderItems(orderDto.getOrderItems().stream().map(item -> {
            String productVariantId = item.getProductVariantId();
            if (productVariantId == null || productVariantId.isEmpty()) {
                throw new IllegalArgumentException("Product variant ID cannot be null or empty");
            }

            ProductVariant variant = productVariantService.getProductVariantById(productVariantId);
            if (variant == null) {
                throw new IllegalArgumentException("Invalid product variant ID: " + productVariantId);
            }

            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product variant ID: " + productVariantId);
            }

            return OrderItem.builder()
                    .id(item.getId())
                    .order(order)
                    .productVariant(variant)
                    .price(variant.getPrice())
                    .quantity(item.getQuantity())
                    .size(item.getSize())
                    .build();
        }).collect(Collectors.toList()));

        orderService.createOrder(order);
        return new ResponseEntity<>(OrderDto.fromEntity(order), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> updateOrder(
            @KeycloakUserId @Parameter(hidden = true) String userId,
            @PathVariable String id,
            @Valid @RequestBody OrderDto orderDto) {

        Order order = orderDto.toEntity(userId);
        order.setOrderItems(orderDto.getOrderItems().stream().map(item -> {
            ProductVariant variant = productVariantService.getProductVariantById(item.getProductVariantId());
            if (variant == null) {
                throw new IllegalArgumentException("Invalid product variant ID: " + item.getProductVariantId());
            }

            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for product variant ID: " + item.getProductVariantId());
            }

            return OrderItem.builder()
                    .id(item.getId())
                    .productVariant(variant)
                    .price(variant.getPrice())
                    .quantity(item.getQuantity())
                    .size(item.getSize())
                    .build();
        }).collect(Collectors.toList()));

        Order updatedOrder = orderService.updateOrder(id, order);
        return ResponseEntity.ok(OrderDto.fromEntity(updatedOrder));
    }

    @PutMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponseDto> confirmOrder(
            @PathVariable String id,
            @Valid @RequestBody NotchPayDto.InitiatePaymentRequest.Customer customer) {

        Optional<Order> optionalOrder = orderService.getOrderById(id);
        if (optionalOrder == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Order order = optionalOrder.get();
        if (!"pending".equals(order.getStatus()))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        List<ProductVariant> variants = productVariantService.getProductVariantsByIds(
                order.getOrderItems().stream()
                        .map(item -> item.getProductVariant().getId())
                        .collect(Collectors.toList()));

        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = variants.stream()
                    .filter(pv -> pv.getId().equals(item.getProductVariant().getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid product variant ID in order: " + item.getProductVariant().getId()));

            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for product variant ID: " + variant.getId()
                                + ". Requested: " + item.getQuantity()
                                + ", Available: " + variant.getStockQuantity());
            }
        }

        BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        Payment payment = paymentService.confirmOrder(
                id, amount.doubleValue(), customer.getEmail(), customer.getName(), customer.getPhone());

        return new ResponseEntity<>(PaymentResponseDto.fromEntity(payment), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (!"pending".equals(order.get().getStatus()))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
