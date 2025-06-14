package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, String> {
    Optional<Fixture> findByApiFixtureId(String apiFixtureId);

    List<Fixture> findByLeagueId(String leagueId);

    // Renamed from findByTeam1IdOrTeam2Id to be more descriptive or provide separate methods if needed
    // This query finds fixtures where the given teamId is either team1 or team2
    @Query("SELECT f FROM Fixture f WHERE f.team1.id = :teamId OR f.team2.id = :teamId")
    List<Fixture> findByTeamId(@Param("teamId") String teamId);

    List<Fixture> findByTeam1Id(String team1Id);
    List<Fixture> findByTeam2Id(String team2Id);

    List<Fixture> findByMatchDatetimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Fixture> findByLeagueIdAndMatchDatetimeBetween(String leagueId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Fixture> findByStatus(String status);

    List<Fixture> findByLeagueIdAndStatus(String leagueId, String status);

    Optional<Fixture> findByApiFixtureIdAndApiSource(String apiFixtureId, String apiSource);

    Page<Fixture> findByLeagueApiLeagueId(String leagueApiId, Pageable pageable); // Added for paginated search by league API ID
}
