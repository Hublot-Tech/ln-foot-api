package co.hublots.ln_foot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import co.hublots.ln_foot.models.Payment;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
}
