package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.models.User.ValidRolesEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByKeycloakId(String keycloakId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(ValidRolesEnum role);
    long countByRole(ValidRolesEnum role);
}
