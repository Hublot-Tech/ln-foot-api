package co.hublots.ln_foot.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.Highlight;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, String> {
    Page<Highlight> findByFixture_ApiFixtureId(String fixtureApiFixtureId, Pageable pageable);
    Page<Highlight> findByFixture_Id(String fixtureInternalId, Pageable pageable);
}
