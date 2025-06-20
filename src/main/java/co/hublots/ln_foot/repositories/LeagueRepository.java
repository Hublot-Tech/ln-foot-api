package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, String>, JpaSpecificationExecutor<League> {
    Optional<League> findByApiLeagueId(String apiLeagueId);

    Optional<League> findByApiLeagueIdAndApiSource(String apiLeagueId, String apiSource);
    List<League> findByCountryAndTier(String country, Integer tier);
}
