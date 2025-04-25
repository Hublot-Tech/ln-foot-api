// src/main/java/co/hublots/ln_foot/models/OrderItem.java
package co.hublots.ln_foot.models;

import java.math.BigDecimal;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class OrderItem {
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "colored_product_id")
    private ColoredProduct coloredProduct;

    private BigDecimal price;

    private int quantity;
}