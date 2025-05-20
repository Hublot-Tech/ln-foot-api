package co.hublots.ln_foot.models;

import java.time.LocalDate;

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
@Table(name = "promotion_products")
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class PromotionProduct {
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne
    @JoinColumn(name = "colored_product_id")
    private ColoredProduct coloredProduct;

    private double discountedPrice;
    private LocalDate startDate;
    private LocalDate endDate;
}