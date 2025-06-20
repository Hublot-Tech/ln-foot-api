package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;

public interface FixtureService {
    Page<FixtureDto> listFixtures(String leagueApiId, Pageable pageable);
    Optional<FixtureDto> findFixtureById(String apiFixtureId);
    List<FixtureDto> getUpcomingFixtures(Integer days, String leagueApiId);
    List<FixtureDto> getFixturesByDate(LocalDate date, String leagueApiId);
    FixtureDto createFixture(CreateFixtureDto createDto);
    FixtureDto updateFixture(String apiFixtureId, UpdateFixtureDto updateDto);
    void deleteFixture(String apiFixtureId);
}
