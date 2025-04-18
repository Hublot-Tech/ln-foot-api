package co.hublots.ln_foot.models;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
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
    @UuidGenerator
    private String id;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    // Authorization server (Keycloak) user id
    private String userId;
    private LocalDate orderDate;
    private boolean isCompleted;

}
