package co.hublots.ln_foot.repositories;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.ColoredProduct;

@Repository
public interface ColoredProductRepository extends JpaRepository<ColoredProduct, String> {
    List<ColoredProduct> findByIdIn(List<String> ids);
} 