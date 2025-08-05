package co.hublots.ln_foot.dto;

import java.time.LocalDateTime;

import co.hublots.ln_foot.models.Payment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDto {
    private String id;
    private String orderId;
    private String paymentId;
    private String paymentPageUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentResponseDto fromEntity(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId().toString())
                .orderId(payment.getOrderId())
                .paymentId(payment.getPaymentRef())
                .paymentPageUrl(payment.getPaymentPageUrl())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

}
