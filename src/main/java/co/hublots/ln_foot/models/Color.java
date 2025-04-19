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

@Entity
@Table(name = "colors")
@Data
@Builder
@AllArgsConstructor
public class Color {

    @Id
    @UuidGenerator
    private String id;

    private String name; // e.g., "Red", "Blue", "Green"

    private String colorCode;

    @ManyToMany(mappedBy = "colors")
    private List<Product> products;
}