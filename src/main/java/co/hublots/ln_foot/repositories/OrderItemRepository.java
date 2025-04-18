package co.hublots.ln_foot.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
} 