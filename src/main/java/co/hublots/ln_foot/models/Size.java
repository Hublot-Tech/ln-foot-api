package co.hublots.ln_foot.models;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sizes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name; // e.g., "S", "M", "L", "XL"

    @ManyToMany(mappedBy = "sizes")
    private List<Product> products;
} 