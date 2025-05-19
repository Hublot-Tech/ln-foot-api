package co.hublots.ln_foot.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.PromotionProduct;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, String> {
} 