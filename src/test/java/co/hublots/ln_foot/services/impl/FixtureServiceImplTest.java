package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FixtureServiceImplTest {
    private FixtureServiceImpl fixtureService;

    @BeforeEach
    void setUp() {
        fixtureService = new FixtureServiceImpl();
    }

    @Test
    void listFixtures_returnsEmptyList() {
        List<FixtureDto> result = fixtureService.listFixtures("league1", "2023");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findFixtureById_returnsEmptyOptional() {
        Optional<FixtureDto> result = fixtureService.findFixtureById("fixture1");
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void getUpcomingFixtures_returnsEmptyList() {
        List<FixtureDto> result = fixtureService.getUpcomingFixtures(7, "league1");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getFixturesByDate_returnsEmptyList() {
        List<FixtureDto> result = fixtureService.getFixturesByDate(LocalDate.now(), "league1");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createFixture_returnsMockDto() {
        CreateFixtureDto createDto = CreateFixtureDto.builder()
                .id("apiFootballId123") // External ID
                .leagueId("league1")
                .season("2023")
                .homeTeamId("teamA")
                .awayTeamId("teamB")
                .date(OffsetDateTime.now().plusDays(5))
                .statusShort("NS")
                .statusLong("Not Started")
                .build();
        FixtureDto result = fixtureService.createFixture(createDto);
        assertNotNull(result);
        assertEquals("apiFootballId123", result.getId());
        assertEquals("league1", result.getLeagueId());
        assertEquals("teamA", result.getHomeTeam().getId());
        assertEquals("teamB", result.getAwayTeam().getId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void createFixture_generatesId_whenNotProvidedInDto() {
        CreateFixtureDto createDto = CreateFixtureDto.builder()
                .leagueId("league2")
                .season("2024")
                .homeTeamId("teamC")
                .awayTeamId("teamD")
                .date(OffsetDateTime.now().plusDays(10))
                .build();
        FixtureDto result = fixtureService.createFixture(createDto);
        assertNotNull(result);
        assertNotNull(result.getId()); // Should be generated UUID
        assertNotEquals("apiFootballId123", result.getId()); // Ensure it's not hardcoded
    }


    @Test
    void updateFixture_returnsMockDto() {
        String fixtureId = "fixtureToUpdate";
        UpdateFixtureDto updateDto = UpdateFixtureDto.builder()
                .referee("Mr. Mock Referee")
                .statusShort("1H")
                .statusLong("First Half")
                .goalsHome(1)
                .goalsAway(0)
                .build();
        FixtureDto result = fixtureService.updateFixture(fixtureId, updateDto);
        assertNotNull(result);
        assertEquals(fixtureId, result.getId());
        assertEquals("Mr. Mock Referee", result.getReferee());
        assertEquals(1, result.getGoalsHome());
        assertTrue(result.getLive()); // Based on status "1H"
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void deleteFixture_completesWithoutError() {
        assertDoesNotThrow(() -> fixtureService.deleteFixture("fixtureToDelete"));
    }
}
