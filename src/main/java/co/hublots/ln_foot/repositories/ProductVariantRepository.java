package co.hublots.ln_foot.repositories;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByIdIn(List<String> ids);
    List<ProductVariant> findAllByProduct_Id(String productId);
} 