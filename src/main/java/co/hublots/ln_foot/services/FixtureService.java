package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FixtureService {
    List<FixtureDto> listFixtures(String leagueId, String season);
    Optional<FixtureDto> findFixtureById(String id);
    List<FixtureDto> getUpcomingFixtures(Integer days, String leagueId);
    List<FixtureDto> getFixturesByDate(LocalDate date, String leagueId);
    FixtureDto createFixture(CreateFixtureDto createDto);
    FixtureDto updateFixture(String id, UpdateFixtureDto updateDto);
    void deleteFixture(String id);
}
