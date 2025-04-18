package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.annotations.KeycloakUserId;
import co.hublots.ln_foot.dto.OrderDto;
import co.hublots.ln_foot.models.Order;
import co.hublots.ln_foot.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public List<OrderDto> getAllOrders(@KeycloakUserId String userId) {
        List<Order> Orders = orderService.getAllOrders();
        return Orders.stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("me")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public List<OrderDto> getUserOrders(@KeycloakUserId String userId) {
        List<Order> Orders = orderService.getUserOrders(userId);
        return Orders.stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String id) {
        try {
            Order Order = orderService.getOrderById(id);
            return new ResponseEntity<>(
                    OrderDto.fromEntity(Order),
                    HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody OrderDto orderDto) {
        Order order = orderDto.toEntity();
        orderService.createOrder(order);
        return new ResponseEntity<>(
                OrderDto.fromEntity(order),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable String id,
            @Valid @RequestBody OrderDto orderDto) {
        try {
            Order Order = orderDto.toEntity();
            Order updatedOrder = orderService.updateOrder(id, Order);
            return new ResponseEntity<>(
                    OrderDto.fromEntity(updatedOrder),
                    HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        try {
            orderService.deleteOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
