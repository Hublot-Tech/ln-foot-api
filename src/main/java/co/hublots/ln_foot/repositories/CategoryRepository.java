package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
} 