package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import co.hublots.ln_foot.services.LeagueService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
// import java.util.UUID; // apiFootballId is string, not necessarily UUID

@Service
public class LeagueServiceImpl implements LeagueService {

    @Override
    public List<LeagueDto> listLeagues(String country, String season, String type) {
        // Mock: Filter if a list of leagues was present
        return Collections.emptyList();
    }

    @Override
    public Optional<LeagueDto> findLeagueById(String id) {
        return Optional.empty();
    }

    @Override
    public LeagueDto createLeague(CreateLeagueDto createDto) {
        return LeagueDto.builder()
                .id(createDto.getId()) // Expecting apiFootballId from DTO
                .name(createDto.getName())
                .country(createDto.getCountry())
                .logoUrl(createDto.getLogoUrl())
                .flagUrl(createDto.getFlagUrl())
                .season(createDto.getSeason())
                .type(createDto.getType())
                .fixtures(Collections.emptyList()) // Fixtures usually fetched separately
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public LeagueDto updateLeague(String id, UpdateLeagueDto updateDto) {
        // Assume fetch, then update
        return LeagueDto.builder()
                .id(id)
                .name(updateDto.getName() != null ? updateDto.getName() : "Original League Name")
                .country(updateDto.getCountry() != null ? updateDto.getCountry() : "Original Country")
                .logoUrl(updateDto.getLogoUrl() != null ? updateDto.getLogoUrl() : "http://original.logo/url")
                .flagUrl(updateDto.getFlagUrl() != null ? updateDto.getFlagUrl() : "http://original.flag/url")
                .season(updateDto.getSeason() != null ? updateDto.getSeason() : "2023")
                .type(updateDto.getType() != null ? updateDto.getType() : "League")
                .fixtures(Collections.emptyList()) // Fixtures usually fetched separately or not part of direct update
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public void deleteLeague(String id) {
        System.out.println("Deleting league with id: " + id);
    }
}
