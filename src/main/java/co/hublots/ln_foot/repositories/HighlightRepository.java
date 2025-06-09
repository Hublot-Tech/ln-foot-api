package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, String> {
    List<Highlight> findByFixtureId(String fixtureId);
    // Example: List<Highlight> findByFixtureLeagueId(String leagueId); // If needing highlights for a whole league
    // Example: List<Highlight> findBySource(String source);
}
