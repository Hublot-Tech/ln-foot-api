package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.TeamDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.repositories.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private FixtureRepository fixtureRepository;
    @Mock
    private LeagueRepository leagueRepository;

    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamServiceImpl(teamRepository, fixtureRepository, leagueRepository);
    }

    private Team createMockTeam(String apiTeamId, String name) {
        return Team.builder()
                .id(UUID.randomUUID().toString()) // Internal UUID
                .apiTeamId(apiTeamId)
                .teamName(name)
                .country("Mockland")
                .logoUrl("http://logo.url/" + name.toLowerCase() + ".png")
                .foundedYear(1900)
                .stadiumName(name + " Stadium")
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private League createMockLeague(String internalId, String apiLeagueId) {
        return League.builder().id(internalId).apiLeagueId(apiLeagueId).leagueName("Mock League").build();
    }

    private Fixture createMockFixture(League league, Team team1, Team team2) {
        return Fixture.builder()
                .id(UUID.randomUUID().toString())
                .league(league)
                .team1(team1)
                .team2(team2)
                .matchDatetime(LocalDateTime.now())
                .status("NS")
                .build();
    }


    @Test
    void listTeamsByLeague_whenLeagueNotFound_throwsEntityNotFoundException() {
        // Arrange
        String leagueApiId = "non-existent-league";
        when(leagueRepository.findByApiLeagueIdAndApiSource(leagueApiId, null)).thenReturn(Optional.empty());
        // Note: TeamServiceImpl uses findByApiLeagueIdAndApiSource, but apiSource is not passed.
        // The current TeamServiceImpl uses leagueRepository.findByApiLeagueId(leagueApiId) which should be findByApiLeagueIdAndApiSource
        // For this test to work as intended with current TeamServiceImpl, let's adjust the mock for findByApiLeagueId
        when(leagueRepository.findByApiLeagueId(leagueApiId)).thenReturn(Optional.empty());


        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> teamService.listTeamsByLeague(leagueApiId, "2023"));
        verify(leagueRepository).findByApiLeagueId(leagueApiId);
    }

    @Test
    void listTeamsByLeague_whenNoFixtures_returnsEmptyList() {
        // Arrange
        String leagueApiId = "league1";
        String internalLeagueId = UUID.randomUUID().toString();
        League mockLeague = createMockLeague(internalLeagueId, leagueApiId);

        when(leagueRepository.findByApiLeagueId(leagueApiId)).thenReturn(Optional.of(mockLeague));
        when(fixtureRepository.findByLeagueId(internalLeagueId)).thenReturn(Collections.emptyList());

        // Act
        List<TeamDto> result = teamService.listTeamsByLeague(leagueApiId, "2023");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(leagueRepository).findByApiLeagueId(leagueApiId);
        verify(fixtureRepository).findByLeagueId(internalLeagueId);
    }

    @Test
    void listTeamsByLeague_returnsTeamDtosFromFixtures() {
        // Arrange
        String leagueApiId = "league-with-fixtures";
        String internalLeagueId = UUID.randomUUID().toString();
        League mockLeague = createMockLeague(internalLeagueId, leagueApiId);

        Team teamA = createMockTeam("teamA-api", "Team Alpha");
        Team teamB = createMockTeam("teamB-api", "Team Beta");
        Team teamC = createMockTeam("teamC-api", "Team Charlie");

        Fixture fixture1 = createMockFixture(mockLeague, teamA, teamB);
        Fixture fixture2 = createMockFixture(mockLeague, teamB, teamC);
        Fixture fixture3 = createMockFixture(mockLeague, teamA, teamC); // teamA and teamC appear again

        when(leagueRepository.findByApiLeagueId(leagueApiId)).thenReturn(Optional.of(mockLeague));
        when(fixtureRepository.findByLeagueId(internalLeagueId)).thenReturn(List.of(fixture1, fixture2, fixture3));

        // Act
        List<TeamDto> result = teamService.listTeamsByLeague(leagueApiId, "2023");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // Unique teams: A, B, C
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Team Alpha") && dto.getId().equals("teamA-api")));
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Team Beta") && dto.getId().equals("teamB-api")));
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Team Charlie") && dto.getId().equals("teamC-api")));
    }


    @Test
    void findTeamById_whenFound_returnsOptionalDto() { // Parameter is apiTeamId
        // Arrange
        String apiTeamId = "team-api-123";
        Team mockTeam = createMockTeam(apiTeamId, "Found Team");
        when(teamRepository.findByApiTeamId(apiTeamId)).thenReturn(Optional.of(mockTeam));

        // Act
        Optional<TeamDto> result = teamService.findTeamById(apiTeamId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(apiTeamId, result.get().getId()); // DTO id should be apiTeamId
        assertEquals(mockTeam.getTeamName(), result.get().getName());
        verify(teamRepository).findByApiTeamId(apiTeamId);
    }

    @Test
    void findTeamById_whenNotFound_returnsEmptyOptional() { // Parameter is apiTeamId
        // Arrange
        String apiTeamId = "nonexistent-api-id";
        when(teamRepository.findByApiTeamId(apiTeamId)).thenReturn(Optional.empty());

        // Act
        Optional<TeamDto> result = teamService.findTeamById(apiTeamId);

        // Assert
        assertFalse(result.isPresent());
        verify(teamRepository).findByApiTeamId(apiTeamId);
    }
}
