package co.hublots.ln_foot.models;

import java.math.BigDecimal;
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
@Table(name = "promotions")
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Promotion {
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal discountedPrice;
    private LocalDate startDate;
    private LocalDate endDate;
}