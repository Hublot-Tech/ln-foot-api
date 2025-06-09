package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.SimpleTeamDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;
import co.hublots.ln_foot.services.FixtureService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FixtureServiceImpl implements FixtureService {

    @Override
    public List<FixtureDto> listFixtures(String leagueId, String season) {
        return Collections.emptyList();
    }

    @Override
    public Optional<FixtureDto> findFixtureById(String id) {
        return Optional.empty();
    }

    @Override
    public List<FixtureDto> getUpcomingFixtures(Integer days, String leagueId) {
        return Collections.emptyList();
    }

    @Override
    public List<FixtureDto> getFixturesByDate(LocalDate date, String leagueId) {
        return Collections.emptyList();
    }

    @Override
    public FixtureDto createFixture(CreateFixtureDto createDto) {
        SimpleTeamDto homeTeam = SimpleTeamDto.builder().id(createDto.getHomeTeamId()).name("Mock Home Team").logoUrl("").build();
        SimpleTeamDto awayTeam = SimpleTeamDto.builder().id(createDto.getAwayTeamId()).name("Mock Away Team").logoUrl("").build();

        return FixtureDto.builder()
                .id(createDto.getId() != null ? createDto.getId() : UUID.randomUUID().toString()) // Use provided ID or generate new
                .referee(createDto.getReferee())
                .timezone(createDto.getTimezone())
                .date(createDto.getDate())
                .timestamp(createDto.getTimestamp())
                .venueName(createDto.getVenueName())
                .venueCity(createDto.getVenueCity())
                .statusShort(createDto.getStatusShort())
                .statusLong(createDto.getStatusLong())
                .elapsed(createDto.getElapsed())
                .leagueId(createDto.getLeagueId())
                .season(createDto.getSeason())
                .round(createDto.getRound())
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .goalsHome(createDto.getGoalsHome())
                .goalsAway(createDto.getGoalsAway())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .live(false)
                .build();
    }

    @Override
    public FixtureDto updateFixture(String id, UpdateFixtureDto updateDto) {
        // Assuming a fetch would happen here in a real scenario
        SimpleTeamDto homeTeam = SimpleTeamDto.builder().id("homeTeamId").name("Mock Home Team").logoUrl("").build();
        SimpleTeamDto awayTeam = SimpleTeamDto.builder().id("awayTeamId").name("Mock Away Team").logoUrl("").build();

        return FixtureDto.builder()
                .id(id)
                .referee(updateDto.getReferee() != null ? updateDto.getReferee() : "Original Referee")
                .timezone(updateDto.getTimezone() != null ? updateDto.getTimezone() : "UTC")
                .date(updateDto.getDate() != null ? updateDto.getDate() : OffsetDateTime.now().plusDays(1))
                .timestamp(updateDto.getTimestamp() != null ? updateDto.getTimestamp() : (int) (OffsetDateTime.now().plusDays(1).toEpochSecond()))
                .venueName(updateDto.getVenueName() != null ? updateDto.getVenueName() : "Original Venue")
                .venueCity(updateDto.getVenueCity() != null ? updateDto.getVenueCity() : "Original City")
                .statusShort(updateDto.getStatusShort() != null ? updateDto.getStatusShort() : "NS")
                .statusLong(updateDto.getStatusLong() != null ? updateDto.getStatusLong() : "Not Started")
                .elapsed(updateDto.getElapsed() != null ? updateDto.getElapsed() : 0)
                .leagueId("originalLeagueId") // Not typically updatable
                .season("originalSeason") // Not typically updatable
                .round("originalRound") // Not typically updatable
                .homeTeam(homeTeam) // Simplified, actual team data wouldn't change like this
                .awayTeam(awayTeam) // Simplified
                .goalsHome(updateDto.getGoalsHome())
                .goalsAway(updateDto.getGoalsAway())
                .createdAt(OffsetDateTime.now().minusDays(1)) // Original creation date
                .updatedAt(OffsetDateTime.now())
                .live(updateDto.getStatusShort() != null && List.of("1H", "HT", "2H", "ET", "P", "LIVE").contains(updateDto.getStatusShort().toUpperCase()))
                .build();
    }

    @Override
    public void deleteFixture(String id) {
        System.out.println("Deleting fixture with id: " + id);
    }
}
