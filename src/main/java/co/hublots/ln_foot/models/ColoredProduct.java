package co.hublots.ln_foot.models;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "colored_pro+ducts")
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ColoredProduct {

    @Id
    @UuidGenerator
    private String id;

    private String name; // e.g., "Red T-shirt",
    private String colorCode;

    @Lob
    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}