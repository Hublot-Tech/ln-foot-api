package co.hublots.ln_foot.services.impl;

import org.springframework.util.StringUtils; // Added
import java.util.Collections; // Added
import co.hublots.ln_foot.dto.TeamDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.repositories.TeamRepository;
import co.hublots.ln_foot.services.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
// Removed mid-file imports as they are at the top now
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    // FixtureRepository and LeagueRepository are no longer needed here if listTeamsByLeague is optimized
    // private final FixtureRepository fixtureRepository;
    // private final LeagueRepository leagueRepository;

    private TeamDto mapToDto(Team entity) {
        if (entity == null) {
            return null;
        }
        return TeamDto.builder()
                .id(entity.getApiTeamId()) // Mapping apiTeamId to DTO's id
                .name(entity.getTeamName())
                .country(entity.getCountry())
                .founded(entity.getFoundedYear())
                // .national(entity.getNational()) // 'national' field not in Team entity
                .logoUrl(entity.getLogoUrl())
                .venueName(entity.getStadiumName())
                // venueAddress, venueCity, venueCapacity not in current Team entity
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamDto> listTeamsByLeague(String leagueApiId) {
        if (!StringUtils.hasText(leagueApiId)) {
            throw new IllegalArgumentException("League API ID cannot be null or empty.");
        }
        List<Team> teams = teamRepository.findDistinctTeamsByLeagueApiId(leagueApiId);
        if (teams == null || teams.isEmpty()) {
            return Collections.emptyList();
        }
        return teams.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamDto> findTeamById(String apiTeamId) { // id parameter is apiTeamId
        return teamRepository.findByApiTeamId(apiTeamId).map(this::mapToDto);
    }
}
