package co.hublots.ln_foot.models;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
@Builder
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    // Authorization server (Keycloak) user id
    private UUID userId;
    private LocalDate orderDate;
    private boolean isCompleted;

}
