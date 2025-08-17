package co.hublots.ln_foot.services;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import co.hublots.ln_foot.models.Payment;
import co.hublots.ln_foot.models.User.Customer;

public interface PaymentService {

    Optional<Payment> findById(String id);
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByReference(String reference);

    /**
     * Initiate hosted payment flow:
     * 1. Create or retrieve existing payment for order
     * 2. Return payment with hosted URL for external completion
     * 3. Payment completion handled via webhooks
     *
     * @param orderId       Order identifier
     * @param amount        Amount in minor units (e.g. cents)
     * @param customerEmail Customer email
     * @param customerName  Customer full name
     * @param customerPhone Customer phone number
     * @return Payment entity with hosted URL for external payment
     */
    @Transactional
    Payment initiateHostedPayment(String orderId, double amount, Customer customer, String callbackUrl);
}
