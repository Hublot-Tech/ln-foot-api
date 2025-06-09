package co.hublots.ln_foot.services.impl;

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
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository; // Added
    private final LeagueRepository leagueRepository;   // Added

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
    public List<TeamDto> listTeamsByLeague(String leagueApiId, String season) {
        // Season is not used in this logic yet, as Team/Fixture entities don't store it directly
        // in a way that's easily queryable for this specific purpose without more complex date logic.
        League league = leagueRepository.findByApiLeagueId(leagueApiId)
            .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + leagueApiId + " not found."));

        List<Fixture> fixtures = fixtureRepository.findByLeagueId(league.getId()); // Uses internal league UUID
        if (fixtures.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Team> teams = new HashSet<>();
        for (Fixture fixture : fixtures) {
            if (fixture.getTeam1() != null) {
                teams.add(fixture.getTeam1());
            }
            if (fixture.getTeam2() != null) {
                teams.add(fixture.getTeam2());
            }
        }
        return teams.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamDto> findTeamById(String apiTeamId) { // id parameter is apiTeamId
        return teamRepository.findByApiTeamId(apiTeamId).map(this::mapToDto);
    }
}
