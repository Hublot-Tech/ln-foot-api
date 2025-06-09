package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;

import java.util.List;
import java.util.Optional;

public interface LeagueService {
    List<LeagueDto> listLeagues(String country, String season, String type);
    Optional<LeagueDto> findLeagueById(String id); // id is apiFootballId
    LeagueDto createLeague(CreateLeagueDto createDto);
    LeagueDto updateLeague(String id, UpdateLeagueDto updateDto);
    void deleteLeague(String id);
}
