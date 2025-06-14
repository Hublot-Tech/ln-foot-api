package co.hublots.ln_foot.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;

public interface LeagueService {
    Page<LeagueDto> listLeagues(String country, String type, Pageable pageable);
    Optional<LeagueDto> findLeagueById(String apiLeagueId);
    LeagueDto createLeague(CreateLeagueDto createDto);
    LeagueDto updateLeague(String id, UpdateLeagueDto updateDto);
    void deleteLeague(String id);
}
