package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.*;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.services.LeagueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;
    // No need for FixtureRepository/TeamRepository for basic League CRUD if not creating/linking them here

    // --- Internal DTO Mapping Helpers ---

    private SimpleTeamDto mapTeamToSimpleTeamDto(Team entity) {
        if (entity == null) return null;
        return SimpleTeamDto.builder()
                .id(entity.getApiTeamId()) // Assuming SimpleTeamDto.id refers to apiTeamId for consistency
                .name(entity.getTeamName())
                .logoUrl(entity.getLogoUrl())
                .build();
    }

    private FixtureDto mapFixtureToDto(Fixture entity) {
        if (entity == null) return null;
        return FixtureDto.builder()
                .id(entity.getApiFixtureId()) // Assuming FixtureDto.id refers to apiFixtureId
                .referee(null) // Fixture entity doesn't have referee; keep null or fetch if needed
                .timezone(null) // Fixture entity doesn't have timezone
                .date(entity.getMatchDatetime() != null ? entity.getMatchDatetime().atOffset(ZoneOffset.UTC) : null)
                .timestamp(entity.getMatchDatetime() != null ? (int) entity.getMatchDatetime().toEpochSecond(ZoneOffset.UTC) : null)
                .venueName(entity.getVenueName())
                .venueCity(null) // Fixture entity doesn't have venueCity
                .statusShort(entity.getStatus()) // Assuming status can map to statusShort
                .statusLong(entity.getStatus())  // Or map to a more descriptive long status if available
                .elapsed(null) // Fixture entity doesn't have elapsed
                .leagueId(entity.getLeague() != null ? entity.getLeague().getApiLeagueId() : null)
                // .season() // Fixture entity doesn't have season directly
                .round(entity.getRound())
                .homeTeam(mapTeamToSimpleTeamDto(entity.getTeam1()))
                .awayTeam(mapTeamToSimpleTeamDto(entity.getTeam2()))
                .goalsHome(entity.getGoalsTeam1())
                .goalsAway(entity.getGoalsTeam2())
                // other score details not in Fixture entity
                .live(List.of("LIVE", "1H", "HT", "2H", "ET", "P").contains(entity.getStatus())) // Basic live logic
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private LeagueDto mapToDto(League entity) {
        if (entity == null) {
            return null;
        }
        return LeagueDto.builder()
                .id(entity.getApiLeagueId()) // DTO id is apiLeagueId
                .name(entity.getLeagueName())
                .country(entity.getCountry())
                .logoUrl(entity.getLogoUrl())
                // flagUrl, season, type are not in League entity
                .fixtures(entity.getFixtures() != null ?
                        entity.getFixtures().stream().map(this::mapFixtureToDto).collect(Collectors.toList()) :
                        Collections.emptyList())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateLeagueDto dto, League entity) {
        entity.setApiLeagueId(dto.getId()); // CreateLeagueDto.id is apiLeagueId
        entity.setLeagueName(dto.getName());
        entity.setCountry(dto.getCountry());
        entity.setLogoUrl(dto.getLogoUrl());
        // sportId, apiSource, tier are not in CreateLeagueDto. They might be set by a sync process.
        // flagUrl, season, type from DTO are not in entity.
    }

    private void mapToEntityForUpdate(UpdateLeagueDto dto, League entity) {
        if (dto.getName() != null) {
            entity.setLeagueName(dto.getName());
        }
        if (dto.getCountry() != null) {
            entity.setCountry(dto.getCountry());
        }
        if (dto.getLogoUrl() != null) {
            entity.setLogoUrl(dto.getLogoUrl());
        }
        // flagUrl, season, type from DTO are not in entity.
        // sportId, apiSource, tier are not in UpdateLeagueDto.
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueDto> listLeagues(String country, String season, String type) {
        // Basic findAll. Filtering by country, season, type would require custom repo methods
        // or in-memory filtering if the dataset is small.
        // Example: if (country != null) { return leagueRepository.findByCountry(country)... }
        return leagueRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeagueDto> findLeagueById(String apiLeagueId) { // id is apiLeagueId
        return leagueRepository.findByApiLeagueId(apiLeagueId).map(this::mapToDto);
    }

    @Override
    @Transactional
    public LeagueDto createLeague(CreateLeagueDto createDto) {
        // Check if league with this apiLeagueId already exists
        leagueRepository.findByApiLeagueId(createDto.getId()).ifPresent(l -> {
            throw new IllegalStateException("League with apiLeagueId " + createDto.getId() + " already exists.");
        });
        League league = new League();
        mapToEntityForCreate(createDto, league);
        // apiSource could be set here if known, e.g. league.setApiSource("internal");
        League savedLeague = leagueRepository.save(league);
        return mapToDto(savedLeague);
    }

    @Override
    @Transactional
    public LeagueDto updateLeague(String apiLeagueId, UpdateLeagueDto updateDto) { // id is apiLeagueId
        League league = leagueRepository.findByApiLeagueId(apiLeagueId)
                .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + apiLeagueId + " not found"));
        mapToEntityForUpdate(updateDto, league);
        League updatedLeague = leagueRepository.save(league);
        return mapToDto(updatedLeague);
    }

    @Override
    @Transactional
    public void deleteLeague(String apiLeagueId) { // id is apiLeagueId
        League league = leagueRepository.findByApiLeagueId(apiLeagueId)
                .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + apiLeagueId + " not found before deletion"));
        leagueRepository.delete(league); // delete by entity to ensure cascades if any (though direct deleteById should also work if cascades are set up)
    }
}
