package co.hublots.ln_foot.dto;

import java.time.Instant;

import co.hublots.ln_foot.models.Payment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDto {
    private String id; // UUID as String for serialization
    private String orderId;
    private String paymentId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static PaymentResponseDto fromEntity(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId().toString())
                .orderId(payment.getOrderId())
                .paymentId(payment.getPaymentRef())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

}
