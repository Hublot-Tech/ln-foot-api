package co.hublots.ln_foot.models;

import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "sizes")
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Size {
    @Id
    @UuidGenerator
    private String id;

    private String name; // e.g., "S", "M", "L", "XL"

    @ManyToMany(mappedBy = "sizes")
    private List<Product> products;
} 