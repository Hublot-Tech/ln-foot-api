package co.hublots.ln_foot.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    Optional<Team> findByApiTeamId(String apiTeamId);

    Optional<Team> findByApiTeamIdAndApiSource(String apiTeamId, String apiSource);

    @Query("SELECT DISTINCT t FROM Fixture f " +
            "JOIN f.league l " +
            "LEFT JOIN f.team1 t " +
            "LEFT JOIN f.team2 t2 " +
            "WHERE l.apiLeagueId = :leagueApiId AND (t IS NOT NULL OR t2 IS NOT NULL)")
    List<Team> findDistinctTeamsByLeagueApiId(@Param("leagueApiId") String leagueApiId);
}
