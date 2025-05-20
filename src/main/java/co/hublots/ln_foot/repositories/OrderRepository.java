package co.hublots.ln_foot.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findById(String id);

    @EntityGraph(attributePaths = "orderItems")
    List<Order> findAllByUserId(String userId);
}