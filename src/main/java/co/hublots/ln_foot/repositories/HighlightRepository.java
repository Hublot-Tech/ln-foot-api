package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, String> {
    // List<Highlight> findByFixtureId(String fixtureId); // Old non-paginated version
    Page<Highlight> findByFixture_ApiFixtureId(String fixtureApiFixtureId, Pageable pageable); // New paginated version
    Page<Highlight> findByFixtureId(String fixtureInternalId, Pageable pageable); // If needed by internal fixture ID
    // Example: List<Highlight> findByFixtureLeagueId(String leagueId); // If needing highlights for a whole league
    // Example: List<Highlight> findBySource(String source);
}
