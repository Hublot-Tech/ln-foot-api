package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.TeamDto;

import java.util.List;
import java.util.Optional;

public interface TeamService {
    List<TeamDto> listTeams(Optional<String> leagueApiId);
    Optional<TeamDto> findTeamById(String apiTeamId);
}
