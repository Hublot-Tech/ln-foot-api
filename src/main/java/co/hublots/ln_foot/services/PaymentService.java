package co.hublots.ln_foot.services;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import co.hublots.ln_foot.models.Payment;

public interface PaymentService {

    Optional<Payment> findById(String id);
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByReference(String reference);

    /**
     * Confirm order payment flow:
     * 1. Initiate payment
     * 2. Charge payment immediately if initiation successful
     *
     * @param orderId       Order identifier
     * @param amount        Amount in minor units (e.g. cents)
     * @param customerEmail Customer email
     * @param customerName  Customer full name
     * @return Payment entity with updated status
     */
    @Transactional
    Payment confirmOrder(String orderId, double amount, String customerEmail, String customerName, String customerPhone);
}
