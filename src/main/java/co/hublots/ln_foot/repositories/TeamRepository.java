package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
// import java.util.List; // For other potential finders

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    Optional<Team> findByApiTeamId(String apiTeamId);
    Optional<Team> findByApiTeamIdAndApiSource(String apiTeamId, String apiSource);
    // Example: List<Team> findByCountry(String country);
}
