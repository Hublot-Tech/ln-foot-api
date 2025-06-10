package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.models.enums.FixtureStatus; // Added import
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.SimpleTeamDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.repositories.TeamRepository;
import co.hublots.ln_foot.services.FixtureService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FixtureServiceImpl implements FixtureService {

    private final FixtureRepository fixtureRepository;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    // --- Internal DTO Mapping Helpers ---
    private SimpleTeamDto mapTeamToSimpleTeamDto(Team entity) {
        if (entity == null) return null;
        return SimpleTeamDto.builder()
                .id(entity.getApiTeamId()) // SimpleTeamDto.id is apiTeamId
                .name(entity.getTeamName())
                .logoUrl(entity.getLogoUrl())
                .build();
    }

    private FixtureDto mapToDto(Fixture entity) {
        if (entity == null) return null;
        return FixtureDto.builder()
                .id(entity.getApiFixtureId()) // FixtureDto.id is apiFixtureId
                // referee, timezone, elapsed not in Fixture entity
                .date(entity.getMatchDatetime() != null ? entity.getMatchDatetime().atOffset(ZoneOffset.UTC) : null)
import co.hublots.ln_foot.models.enums.FixtureStatus; // Added import

// ... (inside the class)
    private FixtureDto mapToDto(Fixture entity) {
        if (entity == null) return null;
        FixtureStatus statusEnum = FixtureStatus.fromShortCode(entity.getStatus());
        return FixtureDto.builder()
                .id(entity.getApiFixtureId())
                .date(entity.getMatchDatetime() != null ? entity.getMatchDatetime().atOffset(ZoneOffset.UTC) : null)
                .timestamp(entity.getMatchDatetime() != null ? (int) entity.getMatchDatetime().toEpochSecond(ZoneOffset.UTC) : null)
                .venueName(entity.getVenueName())
                .statusShortCode(statusEnum.getShortCode()) // Use enum
                .statusDescription(statusEnum.getDescription()) // Use enum
                .isLive(statusEnum.isLive()) // Use enum
                .leagueId(entity.getLeague() != null ? entity.getLeague().getApiLeagueId() : null)
                .round(entity.getRound())
                .homeTeam(mapTeamToSimpleTeamDto(entity.getTeam1()))
                .awayTeam(mapTeamToSimpleTeamDto(entity.getTeam2()))
                .goalsHome(entity.getGoalsTeam1())
                .goalsAway(entity.getGoalsTeam2())
                // referee, timezone, elapsed, venueCity, score details not in Fixture entity, remain null in DTO if not set otherwise
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateFixtureDto dto, Fixture entity, League league, Team homeTeam, Team awayTeam) {
        entity.setApiFixtureId(dto.getId()); // CreateFixtureDto.id is apiFixtureId
        entity.setLeague(league);
        entity.setTeam1(homeTeam);
        entity.setTeam2(awayTeam);
        entity.setMatchDatetime(dto.getDate() != null ? dto.getDate().toLocalDateTime() : null);
        entity.setStatus(dto.getStatusShort() != null ? dto.getStatusShort() : "SCHEDULED"); // Default status
        entity.setRound(dto.getRound());
        entity.setVenueName(dto.getVenueName());
        entity.setGoalsTeam1(dto.getGoalsHome());
        entity.setGoalsTeam2(dto.getGoalsAway());
        // apiSource could be set here, e.g., "internal" or from DTO if available
        // Other fields from CreateFixtureDto (referee, timezone, venueCity, elapsed, other scores) are not in Fixture entity.
    }

    private void mapToEntityForUpdate(UpdateFixtureDto dto, Fixture entity) {
        if (dto.getDate() != null) {
            entity.setMatchDatetime(dto.getDate().toLocalDateTime());
        }
        if (dto.getStatusShort() != null) {
            entity.setStatus(dto.getStatusShort());
        }
        if (dto.getVenueName() != null) {
            entity.setVenueName(dto.getVenueName());
        }
        if (dto.getGoalsHome() != null) {
            entity.setGoalsTeam1(dto.getGoalsHome());
        }
        if (dto.getGoalsAway() != null) {
            entity.setGoalsTeam2(dto.getGoalsAway());
        }
        // Other fields from UpdateFixtureDto (referee, timezone, venueCity, elapsed, other scores, round)
        // are not in Fixture entity or typically not updatable this way (e.g. round).
    }

    // --- Service Methods ---

    // ... (other methods) // Imports moved to top

    @Override
    @Transactional(readOnly = true)
    public Page<FixtureDto> listFixtures(String leagueApiId, Pageable pageable) { // Changed signature
        Page<Fixture> fixturePage;
        if (leagueApiId != null && !leagueApiId.isEmpty()) {
            // Assuming the repository method findByLeagueApiLeagueId handles the case where league might not exist
            // by returning an empty page, or we trust leagueApiId is valid from prior checks/context.
            // If strict check needed:
            // League league = leagueRepository.findByApiLeagueId(leagueApiId)
            // .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + leagueApiId + " not found."));
            // fixturePage = fixtureRepository.findByLeagueId(league.getId(), pageable); // if using internal ID
            fixturePage = fixtureRepository.findByLeagueApiLeagueId(leagueApiId, pageable);
        } else {
            fixturePage = fixtureRepository.findAll(pageable);
        }
        return fixturePage.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FixtureDto> findFixtureById(String apiFixtureId) {
        return fixtureRepository.findByApiFixtureId(apiFixtureId).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FixtureDto> getUpcomingFixtures(Integer days, String leagueApiId) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(days != null ? days : 7);
        List<Fixture> fixtures;
        if (leagueApiId != null && !leagueApiId.isEmpty()) {
            League league = leagueRepository.findByApiLeagueId(leagueApiId)
                .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + leagueApiId + " not found for upcoming fixtures."));
            // The repository method findByLeagueIdAndMatchDatetimeBetween is not paginated.
            // If this list can be large, it should also be paginated or limited.
            // For now, keeping as List, but noting this for potential future enhancement if needed.
            fixtures = fixtureRepository.findByLeagueIdAndMatchDatetimeBetween(league.getId(), startDate, endDate);
        } else {
            fixtures = fixtureRepository.findByMatchDatetimeBetween(startDate, endDate);
        }
        return fixtures.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FixtureDto> getFixturesByDate(LocalDate date, String leagueApiId) { // Renamed leagueId to leagueApiId for consistency
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.atTime(LocalTime.MAX);
        List<Fixture> fixtures;
         if (leagueApiId != null && !leagueApiId.isEmpty()) {
            League league = leagueRepository.findByApiLeagueId(leagueApiId)
                .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + leagueApiId + " not found for fixtures by date."));
            // Similar to getUpcomingFixtures, this is not paginated.
            fixtures = fixtureRepository.findByLeagueIdAndMatchDatetimeBetween(league.getId(), startDate, endDate);
        } else {
            fixtures = fixtureRepository.findByMatchDatetimeBetween(startDate, endDate);
        }
        return fixtures.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FixtureDto createFixture(CreateFixtureDto createDto) {
        fixtureRepository.findByApiFixtureId(createDto.getId()).ifPresent(f -> {
            throw new IllegalStateException("Fixture with apiFixtureId " + createDto.getId() + " already exists.");
        });

        League league = leagueRepository.findByApiLeagueId(createDto.getLeagueId())
                .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + createDto.getLeagueId() + " not found"));
        Team homeTeam = teamRepository.findByApiTeamId(createDto.getHomeTeamId())
                .orElseThrow(() -> new EntityNotFoundException("Home Team with apiTeamId " + createDto.getHomeTeamId() + " not found"));
        Team awayTeam = teamRepository.findByApiTeamId(createDto.getAwayTeamId())
                .orElseThrow(() -> new EntityNotFoundException("Away Team with apiTeamId " + createDto.getAwayTeamId() + " not found"));

        Fixture fixture = new Fixture();
        mapToEntityForCreate(createDto, fixture, league, homeTeam, awayTeam);
        // Consider setting apiSource if available in DTO or context
        // fixture.setApiSource("internal");
        Fixture savedFixture = fixtureRepository.save(fixture);
        return mapToDto(savedFixture);
    }

    @Override
    @Transactional
    public FixtureDto updateFixture(String apiFixtureId, UpdateFixtureDto updateDto) { // id is apiFixtureId
        Fixture fixture = fixtureRepository.findByApiFixtureId(apiFixtureId)
                .orElseThrow(() -> new EntityNotFoundException("Fixture with apiFixtureId " + apiFixtureId + " not found"));
        mapToEntityForUpdate(updateDto, fixture);
        Fixture updatedFixture = fixtureRepository.save(fixture);
        return mapToDto(updatedFixture);
    }

    @Override
    @Transactional
    public void deleteFixture(String apiFixtureId) { // id is apiFixtureId
        Fixture fixture = fixtureRepository.findByApiFixtureId(apiFixtureId)
                .orElseThrow(() -> new EntityNotFoundException("Fixture with apiFixtureId " + apiFixtureId + " not found for deletion."));
        fixtureRepository.delete(fixture);
    }
}
