package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
} 