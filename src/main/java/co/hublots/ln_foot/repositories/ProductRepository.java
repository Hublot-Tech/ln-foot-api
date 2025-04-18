package co.hublots.ln_foot.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
}
