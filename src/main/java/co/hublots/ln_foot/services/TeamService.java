package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.TeamDto;

import java.util.List;
import java.util.Optional;

public interface TeamService {
    List<TeamDto> listTeamsByLeague(String leagueId, String season);
    Optional<TeamDto> findTeamById(String id); // id is apiFootballId
    // No CUD operations based on tRPC router for Teams directly, managed via sync.
}
