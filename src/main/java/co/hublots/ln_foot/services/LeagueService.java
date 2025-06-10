package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeagueService {
    Page<LeagueDto> listLeagues(String country, String type, Pageable pageable); // Removed season, added Pageable
    Optional<LeagueDto> findLeagueById(String apiLeagueId); // id is apiFootballId, renamed for clarity
    LeagueDto createLeague(CreateLeagueDto createDto);
    LeagueDto updateLeague(String id, UpdateLeagueDto updateDto);
    void deleteLeague(String id);
}
