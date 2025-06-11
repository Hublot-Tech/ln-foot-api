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
    // FixtureRepository and LeagueRepository mocks are no longer needed for listTeamsByLeague
    // @Mock private FixtureRepository fixtureRepository;
    // @Mock private LeagueRepository leagueRepository;

    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        // Updated constructor call
        teamService = new TeamServiceImpl(teamRepository);
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
    void listTeamsByLeague_leagueApiIdNullOrBlank_throwsIllegalArgumentException() {
        Exception eNull = assertThrows(IllegalArgumentException.class, () -> teamService.listTeamsByLeague(null));
        assertEquals("League API ID cannot be null or empty.", eNull.getMessage());

        Exception eBlank = assertThrows(IllegalArgumentException.class, () -> teamService.listTeamsByLeague("  "));
        assertEquals("League API ID cannot be null or empty.", eBlank.getMessage());

        verify(teamRepository, never()).findDistinctTeamsByLeagueApiId(anyString());
    }

    @Test
    void listTeamsByLeague_whenRepositoryReturnsEmpty_returnsEmptyList() {
        // Arrange
        String leagueApiId = "league1";
        when(teamRepository.findDistinctTeamsByLeagueApiId(leagueApiId)).thenReturn(Collections.emptyList());

        // Act
        List<TeamDto> result = teamService.listTeamsByLeague(leagueApiId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(teamRepository).findDistinctTeamsByLeagueApiId(leagueApiId);
    }

    @Test
    void listTeamsByLeague_returnsMappedTeamDtos() {
        // Arrange
        String leagueApiId = "league-with-teams";
        Team teamA = createMockTeam("teamA-api", "Team Alpha");
        Team teamB = createMockTeam("teamB-api", "Team Beta");

        when(teamRepository.findDistinctTeamsByLeagueApiId(leagueApiId)).thenReturn(List.of(teamA, teamB));

        // Act
        List<TeamDto> result = teamService.listTeamsByLeague(leagueApiId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Team Alpha") && dto.getId().equals("teamA-api")));
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Team Beta") && dto.getId().equals("teamB-api")));
        verify(teamRepository).findDistinctTeamsByLeagueApiId(leagueApiId);
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
