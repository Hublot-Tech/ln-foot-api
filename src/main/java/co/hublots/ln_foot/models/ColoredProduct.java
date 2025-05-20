package co.hublots.ln_foot.models;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "colored_products")
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ColoredProduct {

    @Id
    @UuidGenerator
    private String id;

    private String size;
    private double price;
    private int stockQuantity;
    private String colorCode;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToMany
    @Builder.Default
    @JoinTable(name = "colored_product_sizes", joinColumns = @JoinColumn(name = "colored_product_id"), inverseJoinColumns = @JoinColumn(name = "size_id"))
    private List<Size> sizes = List.of();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}