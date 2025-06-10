package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.TeamDto;

import java.util.List;
import java.util.Optional;

public interface TeamService {
    List<TeamDto> listTeamsByLeague(String leagueApiId); // Removed season parameter
    Optional<TeamDto> findTeamById(String apiTeamId); // id is apiFootballId, renamed param for clarity
    // No CUD operations based on tRPC router for Teams directly, managed via sync.
}
