package co.hublots.ln_foot.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.Size;

@Repository
public interface SizeRepository extends JpaRepository<Size, String> {
    Optional<Size> findByNameIgnoreCase(String name);
    List<Size> findAllByNameInIgnoreCase(Set<String> names);
}