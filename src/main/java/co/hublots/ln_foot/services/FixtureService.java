package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page; // Added for Page
import org.springframework.data.domain.Pageable; // Added for Pageable

public interface FixtureService {
    Page<FixtureDto> listFixtures(String leagueApiId, Pageable pageable); // Changed signature
    Optional<FixtureDto> findFixtureById(String apiFixtureId); // Renamed id to apiFixtureId for clarity
    List<FixtureDto> getUpcomingFixtures(Integer days, String leagueApiId); // Renamed leagueId
    List<FixtureDto> getFixturesByDate(LocalDate date, String leagueId);
    FixtureDto createFixture(CreateFixtureDto createDto);
    FixtureDto updateFixture(String id, UpdateFixtureDto updateDto);
    void deleteFixture(String id);
}
