package co.hublots.ln_foot.services;

import org.springframework.transaction.annotation.Transactional;
import co.hublots.ln_foot.models.Payment;

public interface PaymentService {

    /**
     * Confirm order payment flow:
     * 1. Initiate payment
     * 2. Charge payment immediately if initiation successful
     *
     * @param orderId       Order identifier
     * @param amount        Amount in minor units (e.g. cents)
     * @param currency      Currency code (e.g. "USD")
     * @param customerEmail Customer email
     * @param customerName  Customer full name
     * @return Payment entity with updated status
     */
    @Transactional
    Payment confirmOrder(String orderId, int amount, String currency, String customerEmail, String customerName, String customerPhone);
}
