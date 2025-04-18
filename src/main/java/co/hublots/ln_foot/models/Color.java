package co.hublots.ln_foot.models;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "colors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name; // e.g., "Red", "Blue", "Green"

    private String colorCode;

    @ManyToMany(mappedBy = "colors")
    private List<Product> products;
} 