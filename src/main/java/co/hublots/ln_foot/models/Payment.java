package co.hublots.ln_foot.models;


import java.time.Instant;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Payment {
    @Id
    @UuidGenerator
    private String id;

    private String orderId;

    private String paymentRef; // NotchPay payment ID

    private String status;

    private Instant createdAt;

    private Instant updatedAt;

}
