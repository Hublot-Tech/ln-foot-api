package co.hublots.ln_foot.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j; // Added for logging

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

@Slf4j // Added
@Service
@RequiredArgsConstructor
public class LeagueServiceImpl implements LeagueService {

    // private static final Logger log = LoggerFactory.getLogger(LeagueServiceImpl.class); // Replaced by @Slf4j
    private final LeagueRepository leagueRepository;
    // No need for FixtureRepository/TeamRepository for basic League CRUD if not creating/linking them here

    // --- Internal DTO Mapping Helpers ---

    // ... private DTO mapping helpers remain unchanged ...
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
                .statusShortCode(FixtureStatus.fromShortCode(entity.getStatus()).getShortCode()) // Updated to use FixtureStatus
                .statusDescription(FixtureStatus.fromShortCode(entity.getStatus()).getDescription()) // Updated
                .isLive(FixtureStatus.fromShortCode(entity.getStatus()).isLive()) // Updated
                .elapsed(null) // Fixture entity doesn't have elapsed
                .leagueId(entity.getLeague() != null ? entity.getLeague().getApiLeagueId() : null)
                // .season() // Fixture entity doesn't have season directly
                .round(entity.getRound())
                .homeTeam(mapTeamToSimpleTeamDto(entity.getTeam1()))
                .awayTeam(mapTeamToSimpleTeamDto(entity.getTeam2()))
                .goalsHome(entity.getGoalsTeam1())
                .goalsAway(entity.getGoalsTeam2())
                // other score details not in Fixture entity
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
                // .fixtures( ... ) // Removed: fixtures list is no longer part of LeagueDto
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

    // Removed misplaced imports from here

    @Override
    @Transactional(readOnly = true)
    public Page<LeagueDto> listLeagues(String country, String type, Pageable pageable) { // Removed season, added Pageable
        log.debug("Listing leagues with country: [{}], type: [{}], pageable: {}", country, type, pageable);

        Specification<League> spec = Specification.where(null);

        if (StringUtils.hasText(country)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("country"), country));
        }

        // Assuming 'type' might map to 'sportId' or a conceptual 'type' field if it existed.
        // League entity has 'sportId' and 'tier'. 'type' from controller is ambiguous here.
        // If 'type' was meant to be 'sportId':
        // if (StringUtils.hasText(type)) {
        //     spec = spec.and((root, query, cb) -> cb.equal(root.get("sportId"), type));
        // }
        // If 'type' was meant for something like "Cup", "League" and entity had such a field:
        // if (StringUtils.hasText(type)) {
        //     spec = spec.and((root, query, cb) -> cb.equal(root.get("leagueTypeField"), type)); // Assuming 'leagueTypeField'
        // }
        if (StringUtils.hasText(type)) {
            log.warn("Filtering by 'type' ('{}') is not fully implemented as League entity may not have a direct 'type' field. This filter might be ignored or adapted.", type);
            // For now, let's assume 'type' could filter by 'sportId' if that's the intent
             spec = spec.and((root, query, cb) -> cb.equal(root.get("sportId"), type));
        }

        // Season filter is removed as League entity does not have a season field.
        // Filtering leagues by season often implies looking at associated fixtures for that season,
        // which is more complex than a direct attribute filter on League.

        Page<League> leaguePage = leagueRepository.findAll(spec, pageable);
        log.debug("Found {} leagues matching criteria.", leaguePage.getTotalElements());
        return leaguePage.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeagueDto> findLeagueById(String apiLeagueId) {
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
