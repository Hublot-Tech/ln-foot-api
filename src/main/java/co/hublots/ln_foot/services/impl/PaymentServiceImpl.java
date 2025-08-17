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

import co.hublots.ln_foot.dto.NotchPayDto.InitiatePaymentRequest;
import co.hublots.ln_foot.dto.NotchPayDto.InitiatePaymentResponse;
import co.hublots.ln_foot.models.Payment;
import co.hublots.ln_foot.models.User.Customer;
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

    @Override
    @Transactional
    public Payment initiateHostedPayment(String orderId, double amount, Customer customer, String callbackUrl) {
        Optional<Payment> existing = paymentRepository.findByOrderId(orderId)
                .filter(p -> {
                    String status = p.getStatus();
                    return !status.equalsIgnoreCase("failed")
                            && !status.equalsIgnoreCase("expired")
                            && !status.equalsIgnoreCase("cancelled");
                });
        if (existing.isPresent()) {
            return existing.get();
        }
        InitiatePaymentRequest request = InitiatePaymentRequest.builder()
                .amount(amount)
                .currency("XAF")
                .customer(InitiatePaymentRequest.Customer.builder()
                        .name(customer.getName())
                        .email(customer.getEmail())
                        .phone(customer.getPhone())
                        .build())
                .description("Payment for Order #" + orderId)
                .callbackUrl(callbackUrl)
                .reference(orderId)
                .build();
        InitiatePaymentResponse response = initiatePayment(request);
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentRef(response.getTransaction().getReference())
                .paymentPageUrl(response.getAuthorizationUrl())
                .status(response.getTransaction().getStatus())
                .build();
        return paymentRepository.save(payment);
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
        }
        throw new RuntimeException("Failed to initiate payment: HTTP " + response.getStatusCode());
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