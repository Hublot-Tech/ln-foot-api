package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Size;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeRepository extends JpaRepository<Size, String> {
} 