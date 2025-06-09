package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.LeagueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueServiceImplTest {

    @Mock
    private LeagueRepository leagueRepository;
    // No Fixture/Team repositories needed directly as LeagueService only manages League entity,
    // and fixture list mapping is done from League.fixtures (which would be populated if eager or fetched)

    private LeagueServiceImpl leagueService;

    @BeforeEach
    void setUp() {
        leagueService = new LeagueServiceImpl(leagueRepository);
    }

    private League createMockLeague(String internalId, String apiLeagueId, String name, List<Fixture> fixtures) {
        return League.builder()
                .id(internalId) // Internal UUID
                .apiLeagueId(apiLeagueId)
                .leagueName(name)
                .country("Mockland")
                .logoUrl("http://logo.url/league.png")
                .sportId("soccer")
                .tier(1)
                .apiSource("test-source")
                .fixtures(fixtures) // For testing mapping of nested fixtures
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Team createMockTeam(String apiId, String name) { // Helper for Fixture's teams
        return Team.builder().id(UUID.randomUUID().toString()).apiTeamId(apiId).teamName(name).logoUrl("logo").build();
    }

    private Fixture createMockFixture(String apiId, League league, Team t1, Team t2) { // Helper
         return Fixture.builder()
            .id(UUID.randomUUID().toString())
            .apiFixtureId(apiId)
            .league(league)
            .team1(t1)
            .team2(t2)
            .matchDatetime(LocalDateTime.now())
            .status("NS")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }


    @Test
    void listLeagues_returnsListOfDtos() {
        // Arrange
        League mockLeague1 = createMockLeague(UUID.randomUUID().toString(), "L1_API", "League One", Collections.emptyList());
        League mockLeague2 = createMockLeague(UUID.randomUUID().toString(), "L2_API", "League Two", Collections.emptyList());
        when(leagueRepository.findAll()).thenReturn(List.of(mockLeague1, mockLeague2));

        // Act
        List<LeagueDto> result = leagueService.listLeagues(null, null, null); // Params currently not used by repo call

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("L1_API", result.get(0).getId()); // DTO id is apiLeagueId
        assertEquals("League One", result.get(0).getName());
        verify(leagueRepository).findAll();
    }

    @Test
    void findLeagueById_whenFound_returnsOptionalDtoWithFixtures() { // Param is apiLeagueId
        // Arrange
        String apiLeagueId = "league-api-123";
        String internalId = UUID.randomUUID().toString();

        Team teamA = createMockTeam("teamA_api", "Team A");
        Team teamB = createMockTeam("teamB_api", "Team B");
        League tempLeague = League.builder().id(internalId).apiLeagueId(apiLeagueId).build(); // Temporary for fixture creation
        Fixture mockFixture = createMockFixture("fix1_api", tempLeague, teamA, teamB);

        League mockLeague = createMockLeague(internalId, apiLeagueId, "Test League", List.of(mockFixture));
        // Ensure fixture's league points back to the main mockLeague for mapping consistency
        mockFixture.setLeague(mockLeague);


        when(leagueRepository.findByApiLeagueId(apiLeagueId)).thenReturn(Optional.of(mockLeague));

        // Act
        Optional<LeagueDto> result = leagueService.findLeagueById(apiLeagueId);

        // Assert
        assertTrue(result.isPresent());
        LeagueDto dto = result.get();
        assertEquals(apiLeagueId, dto.getId());
        assertEquals("Test League", dto.getName());
        assertNotNull(dto.getFixtures());
        assertEquals(1, dto.getFixtures().size());
        assertEquals("fix1_api", dto.getFixtures().get(0).getId()); // FixtureDto id is apiFixtureId
        assertEquals("teamA_api", dto.getFixtures().get(0).getHomeTeam().getId());

        verify(leagueRepository).findByApiLeagueId(apiLeagueId);
    }

    @Test
    void findLeagueById_whenNotFound_returnsEmptyOptional() { // Param is apiLeagueId
        // Arrange
        String apiLeagueId = "nonexistent-api-id";
        when(leagueRepository.findByApiLeagueId(apiLeagueId)).thenReturn(Optional.empty());

        // Act
        Optional<LeagueDto> result = leagueService.findLeagueById(apiLeagueId);

        // Assert
        assertFalse(result.isPresent());
        verify(leagueRepository).findByApiLeagueId(apiLeagueId);
    }

    @Test
    void createLeague_savesAndReturnsDto() {
        // Arrange
        CreateLeagueDto createDto = CreateLeagueDto.builder()
                .id("new-league-api") // This is apiLeagueId
                .name("New Super League")
                .country("Utopia")
                .logoUrl("http://logo.url/nsl.png")
                .build();

        when(leagueRepository.findByApiLeagueId(createDto.getId())).thenReturn(Optional.empty());

        ArgumentCaptor<League> leagueCaptor = ArgumentCaptor.forClass(League.class);
        when(leagueRepository.save(leagueCaptor.capture())).thenAnswer(invocation -> {
            League savedLeague = invocation.getArgument(0);
            savedLeague.setId(UUID.randomUUID().toString()); // Simulate internal ID generation
            savedLeague.setCreatedAt(LocalDateTime.now());
            savedLeague.setUpdatedAt(LocalDateTime.now());
            // apiSource should be set by service
            return savedLeague;
        });


        // Act
        LeagueDto resultDto = leagueService.createLeague(createDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(createDto.getId(), resultDto.getId()); // DTO id is apiLeagueId
        assertEquals(createDto.getName(), resultDto.getName());
        assertEquals(createDto.getCountry(), resultDto.getCountry());

        League captured = leagueCaptor.getValue();
        assertEquals(createDto.getId(), captured.getApiLeagueId());
        assertEquals(createDto.getName(), captured.getLeagueName());
        assertEquals("RapidAPIFootballV1", captured.getApiSource()); // Check if default source set

        verify(leagueRepository).findByApiLeagueId(createDto.getId());
        verify(leagueRepository).save(any(League.class));
    }

    @Test
    void createLeague_whenApiIdExists_throwsIllegalStateException() {
        CreateLeagueDto createDto = CreateLeagueDto.builder().id("existing-api-id").name("Test").build();
        when(leagueRepository.findByApiLeagueId("existing-api-id"))
            .thenReturn(Optional.of(new League())); // Simulate league already exists

        assertThrows(IllegalStateException.class, () -> leagueService.createLeague(createDto));
        verify(leagueRepository, never()).save(any(League.class));
    }


    @Test
    void updateLeague_whenFound_updatesAndReturnsDto() { // Param is apiLeagueId
        // Arrange
        String apiLeagueId = "league-to-update";
        String internalId = UUID.randomUUID().toString();
        League existingLeague = createMockLeague(internalId, apiLeagueId, "Old Name", Collections.emptyList());
        when(leagueRepository.findByApiLeagueId(apiLeagueId)).thenReturn(Optional.of(existingLeague));

        UpdateLeagueDto updateDto = UpdateLeagueDto.builder().name("Updated League Name").build();

        when(leagueRepository.save(any(League.class))).thenAnswer(invocation -> {
            League leagueToSave = invocation.getArgument(0);
            leagueToSave.setUpdatedAt(LocalDateTime.now()); // Simulate @UpdateTimestamp
            return leagueToSave;
        });

        // Act
        LeagueDto resultDto = leagueService.updateLeague(apiLeagueId, updateDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(apiLeagueId, resultDto.getId());
        assertEquals("Updated League Name", resultDto.getName());
        assertTrue(resultDto.getUpdatedAt().isAfter(existingLeague.getUpdatedAt().atOffset(ZoneOffset.UTC).minusSeconds(1)));

        verify(leagueRepository).findByApiLeagueId(apiLeagueId);
        ArgumentCaptor<League> leagueCaptor = ArgumentCaptor.forClass(League.class);
        verify(leagueRepository).save(leagueCaptor.capture());
        assertEquals("Updated League Name", leagueCaptor.getValue().getLeagueName());
    }

    @Test
    void updateLeague_whenNotFound_throwsEntityNotFoundException() { // Param is apiLeagueId
        // Arrange
        String apiLeagueId = "nonexistent-api-id";
        UpdateLeagueDto updateDto = new UpdateLeagueDto();
        when(leagueRepository.findByApiLeagueId(apiLeagueId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> leagueService.updateLeague(apiLeagueId, updateDto));
        verify(leagueRepository).findByApiLeagueId(apiLeagueId);
        verify(leagueRepository, never()).save(any(League.class));
    }

    @Test
    void deleteLeague_whenFound_deletesLeague() { // Param is apiLeagueId
        // Arrange
        String apiLeagueId = "league-to-delete";
        League mockLeague = createMockLeague(UUID.randomUUID().toString(), apiLeagueId, "To Delete", Collections.emptyList());
        when(leagueRepository.findByApiLeagueId(apiLeagueId)).thenReturn(Optional.of(mockLeague));
        doNothing().when(leagueRepository).delete(mockLeague);

        // Act
        assertDoesNotThrow(() -> leagueService.deleteLeague(apiLeagueId));

        // Assert
        verify(leagueRepository).findByApiLeagueId(apiLeagueId);
        verify(leagueRepository).delete(mockLeague);
    }

    @Test
    void deleteLeague_whenNotFound_throwsEntityNotFoundException() { // Param is apiLeagueId
        // Arrange
        String apiLeagueId = "nonexistent-api-id";
        when(leagueRepository.findByApiLeagueId(apiLeagueId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> leagueService.deleteLeague(apiLeagueId));
        verify(leagueRepository).findByApiLeagueId(apiLeagueId);
        verify(leagueRepository, never()).delete(any(League.class));
    }
}
