package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
// import java.util.List; // For other potential finders

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List; // Added for List return type

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    Optional<Team> findByApiTeamId(String apiTeamId);
    Optional<Team> findByApiTeamIdAndApiSource(String apiTeamId, String apiSource);

    @Query("SELECT DISTINCT t FROM Fixture f JOIN f.team1 t WHERE f.league.apiLeagueId = :leagueApiId " +
           "UNION " +
           "SELECT DISTINCT t2 FROM Fixture f2 JOIN f2.team2 t2 WHERE f2.league.apiLeagueId = :leagueApiId")
    List<Team> findDistinctTeamsByLeagueApiId(@Param("leagueApiId") String leagueApiId);

    // Example: List<Team> findByCountry(String country);
}
