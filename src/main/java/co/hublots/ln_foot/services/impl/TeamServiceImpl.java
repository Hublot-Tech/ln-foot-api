package co.hublots.ln_foot.services.impl;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import co.hublots.ln_foot.dto.TeamDto;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.TeamRepository;
import co.hublots.ln_foot.services.TeamService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    /**
     * Maps a Team entity to a TeamDto.
     *
     * @param entity the Team entity to map
     * @return the mapped TeamDto, or null if the entity is null
     */
    private TeamDto mapToDto(Team entity) {
        if (entity == null) {
            return null;
        }
        return TeamDto.builder()
                .id(entity.getApiTeamId())
                .name(entity.getTeamName())
                .country(entity.getCountry())
                .founded(entity.getFoundedYear())
                .logoUrl(entity.getLogoUrl())
                .venueName(entity.getStadiumName())
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
