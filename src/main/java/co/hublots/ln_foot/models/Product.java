package co.hublots.ln_foot.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Entity
@Table(name = "products", schema = "lnfoot_api")
@Data
@Builder
@AllArgsConstructor
public class Product {
    @Id
    @UuidGenerator
    private String id;

    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;

    private String imageUrl;

    @ManyToMany
    @Builder.Default
    @JoinTable(name = "product_categories",  joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories = List.of();

    @ManyToMany
    @Builder.Default
    @JoinTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "size_id"))
    private List<Size> sizes = List.of();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
