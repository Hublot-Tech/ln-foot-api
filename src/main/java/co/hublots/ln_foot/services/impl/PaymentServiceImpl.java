package co.hublots.ln_foot.services.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import co.hublots.ln_foot.dto.NotchPayDto.ChargePaymentRequest;
import co.hublots.ln_foot.dto.NotchPayDto.ChargePaymentResponse;
import co.hublots.ln_foot.dto.NotchPayDto.InitiatePaymentRequest;
import co.hublots.ln_foot.dto.NotchPayDto.InitiatePaymentResponse;
import co.hublots.ln_foot.models.Payment;
import co.hublots.ln_foot.repositories.PaymentRepository;
import co.hublots.ln_foot.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notchpay.api-key}")
    private String apiKey;

    @Value("${notchpay.api-base-url}")
    private String baseUrl;

    private String getInitiateUrl() {
        return baseUrl + "/payments";
    }

    private String getChargeUrl(String reference) {
        return baseUrl + "/payments/" + reference;
    }

    @Override
    @Transactional
    public Payment confirmOrder(String orderId, double amount, String customerEmail, String customerName,
            String customerPhone) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .filter(existing -> {
                    String status = existing.getStatus();
                    return !status.equalsIgnoreCase("failed")
                            && !status.equalsIgnoreCase("expired")
                            && !status.equalsIgnoreCase("cancelled");
                })
                .orElseGet(() -> {
                    InitiatePaymentRequest initiateReq = InitiatePaymentRequest.builder()
                            .amount(amount)
                            .currency("XAF")
                            .customer(InitiatePaymentRequest.Customer.builder()
                                    .name(customerName)
                                    .email(customerEmail)
                                    .phone(customerPhone)
                                    .build())
                            .description("Payment for Order #" + orderId)
                            .reference(orderId)
                            .build();

                    InitiatePaymentResponse initiateResp = initiatePayment(initiateReq);

                    Payment newPayment = Payment.builder()
                            .orderId(orderId)
                            .paymentRef(initiateResp.getTransaction().getReference())
                            .status(initiateResp.getTransaction().getStatus())
                            .build();

                    return paymentRepository.save(newPayment);
                });

        switch (payment.getStatus()) {
            case "pending":
                ChargePaymentRequest chargeReq = ChargePaymentRequest.builder()
                        .channel("cm.mobile")
                        .data(ChargePaymentRequest.ChargeData.builder()
                                .phone(customerPhone)
                                .build())
                        .build();

                ChargePaymentResponse chargeResp = chargePayment(payment.getPaymentRef(), chargeReq);

                payment.setStatus(chargeResp.getTransaction().getStatus());
                paymentRepository.save(payment);
                break;

            case "processing":
                log.info("Payment for order {} is processing, waiting for completion.", orderId);
                break;

            case "complete":
                log.info("Payment for order {} completed.", orderId);
                break;

            case "failed":
            case "canceled":
            case "expired":
                log.warn("Payment for order {} is in status {} - no further action.", orderId, payment.getStatus());
                break;

            default:
                log.warn("Unknown payment status {} for order {}.", payment.getStatus(), orderId);
        }

        return payment;
    }

    private InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        HttpEntity<InitiatePaymentRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<InitiatePaymentResponse> response = restTemplate.exchange(
                getInitiateUrl(),
                HttpMethod.POST,
                entity,
                InitiatePaymentResponse.class);

        if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to initiate payment: HTTP " + response.getStatusCode());
        }
    }

    private ChargePaymentResponse chargePayment(String reference, ChargePaymentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        HttpEntity<ChargePaymentRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ChargePaymentResponse> response = restTemplate.exchange(
                getChargeUrl(reference),
                HttpMethod.POST,
                entity,
                ChargePaymentResponse.class);

        if (response.getStatusCode() == HttpStatus.ACCEPTED && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to charge payment: HTTP " + response.getStatusCode());
        }
    }

    @Override
    public Optional<Payment> findById(String id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<Payment> findByReference(String reference) {
        return paymentRepository.findByPaymentRef(reference);
    }
}
