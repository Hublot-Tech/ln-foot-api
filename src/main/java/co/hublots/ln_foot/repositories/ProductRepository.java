package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
