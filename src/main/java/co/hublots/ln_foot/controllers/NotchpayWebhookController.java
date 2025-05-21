package co.hublots.ln_foot.controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.hublots.ln_foot.models.Payment;
import co.hublots.ln_foot.repositories.OrderRepository;
import co.hublots.ln_foot.repositories.PaymentRepository;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class NotchpayWebhookController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${notchpay.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/notchpay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "x-notch-signature", required = false) String signature) {
        if (signature == null || !verifySignature(payload, signature, webhookSecret)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Parse the event JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode event = mapper.readTree(payload);
            String eventType = event.get("type").asText();
            JsonNode eventData = event.get("data");

            // Handle the event
            switch (eventType) {
                case "payment.complete": {
                    log.info("Payment " + eventData.get("id").asText() + " completed");

                    // Update the payment status in your database
                    Optional<Payment> payment = paymentRepository.findByPaymentRef(eventData.get("reference").asText());
                    if (payment.isPresent()) {
                        Payment p = payment.get();
                        p.setStatus("completed");
                        paymentRepository.save(p);
                        orderRepository.findById(p.getOrderId()).ifPresent(order -> {
                            order.setStatus("completed");
                            orderRepository.save(order);
                        });
                    } else {
                        log.info("Payment not found for reference: " + eventData.get("reference").asText());
                    }
                }
                    break;
                case "payment.failed": {
                    log.info("Payment " + eventData.get("id").asText() + " failed");

                    // Update the payment status in your database
                    Optional<Payment> payment = paymentRepository.findByPaymentRef(eventData.get("reference").asText());
                    if (payment.isPresent()) {
                        Payment p = payment.get();
                        p.setStatus("failed");
                        paymentRepository.save(p);
                        orderRepository.findById(p.getOrderId()).ifPresent(order -> {
                            order.setStatus("failed");
                            orderRepository.save(order);
                        });
                    } else {
                        log.info("Payment not found for reference: " + eventData.get("reference").asText());
                    }
                }
                    break;
                default:
                    log.info("Unhandled event type: " + eventType);
            }

            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing error");
        }
    }

    private boolean verifySignature(String payload, String signature, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hashBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = bytesToHex(hashBytes);

            return MessageDigest.isEqual(
                    computedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // lower-case hex
        }
        return sb.toString();
    }
}
