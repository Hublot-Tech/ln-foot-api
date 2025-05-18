package co.hublots.ln_foot.models;


import java.time.Instant;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @UuidGenerator
    private Long id;

    private String orderId;

    private String paymentId; // NotchPay payment ID

    private String status;

    private Instant createdAt;

    private Instant updatedAt;

}
