package co.hublots.ln_foot.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.Size;

@Repository
public interface SizeRepository extends JpaRepository<Size, String> {
    // case‐insensitive
    Optional<Size> findByNameIgnoreCase(String name);
}