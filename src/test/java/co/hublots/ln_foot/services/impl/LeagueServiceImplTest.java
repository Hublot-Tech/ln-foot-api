package co.hublots.ln_foot.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.LeagueRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class LeagueServiceImplTest {

    @Mock
    private LeagueRepository leagueRepository;
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
                .matchDatetime(OffsetDateTime.now())
                .status("NS")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listLeagues_noFilters_returnsPagedDtos() { // Updated test
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        League mockLeague1 = createMockLeague(UUID.randomUUID().toString(), "L1_API", "League One",
                Collections.emptyList());
        League mockLeague2 = createMockLeague(UUID.randomUUID().toString(), "L2_API", "League Two",
                Collections.emptyList());
        Page<League> leaguePage = new PageImpl<>(List.of(mockLeague1, mockLeague2), pageable, 2);

        when(leagueRepository.findAll(ArgumentMatchers.<Specification<League>>any(), eq(pageable)))
                .thenReturn(leaguePage);

        // Act
        Page<LeagueDto> result = leagueService.listLeagues(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("L1_API", result.getContent().get(0).getId());
        assertEquals("League One", result.getContent().get(0).getName());
        verify(leagueRepository).findAll(ArgumentMatchers.<Specification<League>>any(), eq(pageable));
    }

    @Test
    void listLeagues_withCountryFilter_returnsFilteredPagedDtos() { // New test for filtering
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String countryFilter = "Mockland";
        League mockLeague1 = createMockLeague(UUID.randomUUID().toString(), "L1_API", "League One",
                Collections.emptyList());
        mockLeague1.setCountry(countryFilter);
        Page<League> leaguePage = new PageImpl<>(List.of(mockLeague1), pageable, 1);

        // ArgumentCaptor for Specification can be complex, so we trust the service
        // builds it.
        // We verify the interaction with the repository.
        when(leagueRepository.findAll(ArgumentMatchers.<Specification<League>>any(), eq(pageable)))
                .thenReturn(leaguePage);

        // Act
        Page<LeagueDto> result = leagueService.listLeagues(countryFilter, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(countryFilter, result.getContent().get(0).getCountry());
        verify(leagueRepository).findAll(ArgumentMatchers.<Specification<League>>any(), eq(pageable));
    }

    @Test
    void findLeagueById_whenFound_returnsOptionalDtoWithFixtures() { // Param is apiLeagueId
        // Arrange
        String apiLeagueId = "league-api-123";
        String internalId = UUID.randomUUID().toString();

        Team teamA = createMockTeam("teamA_api", "Team A");
        Team teamB = createMockTeam("teamB_api", "Team B");
        League tempLeague = League.builder().id(internalId).apiLeagueId(apiLeagueId).build(); // Temporary for fixture
                                                                                              // creation
        Fixture mockFixture = createMockFixture("fix1_api", tempLeague, teamA, teamB);

        League mockLeague = createMockLeague(internalId, apiLeagueId, "Test League", List.of(mockFixture));
        // Ensure fixture's league points back to the main mockLeague for mapping
        // consistency
        mockFixture.setLeague(mockLeague);

        when(leagueRepository.findByApiLeagueId(apiLeagueId)).thenReturn(Optional.of(mockLeague));

        // Act
        Optional<LeagueDto> result = leagueService.findLeagueById(apiLeagueId);

        // Assert
        assertTrue(result.isPresent());
        LeagueDto dto = result.get();
        assertEquals(apiLeagueId, dto.getId());
        assertEquals("Test League", dto.getName());

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
                .apiFootballId("new-league-api") // This is apiLeagueId
                .name("New Super League")
                .country("Utopia")
                .logoUrl("http://logo.url/nsl.png")
                .build();

        when(leagueRepository.findByApiLeagueId(createDto.getApiFootballId())).thenReturn(Optional.empty());

        ArgumentCaptor<League> leagueCaptor = ArgumentCaptor.forClass(League.class);
        when(leagueRepository.save(leagueCaptor.capture())).thenAnswer(invocation -> {
            League savedLeague = invocation.getArgument(0);
            savedLeague.setId(UUID.randomUUID().toString()); // Simulate internal ID generation
            savedLeague.setCreatedAt(LocalDateTime.now());
            savedLeague.setUpdatedAt(LocalDateTime.now());
            savedLeague.setApiSource("RapidAPIFootballV1");
            // apiSource should be set by service
            return savedLeague;
        });

        // Act
        LeagueDto resultDto = leagueService.createLeague(createDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(createDto.getApiFootballId(), resultDto.getId()); // DTO id is apiLeagueId
        assertEquals(createDto.getName(), resultDto.getName());
        assertEquals(createDto.getCountry(), resultDto.getCountry());

        League captured = leagueCaptor.getValue();
        assertEquals(createDto.getApiFootballId(), captured.getApiLeagueId());
        assertEquals(createDto.getName(), captured.getLeagueName());
        assertEquals("RapidAPIFootballV1", captured.getApiSource()); // Check if default source set

        verify(leagueRepository).findByApiLeagueId(createDto.getApiFootballId());
        verify(leagueRepository).save(any(League.class));
    }

    @Test
    void createLeague_whenApiIdExists_throwsIllegalStateException() {
        CreateLeagueDto createDto = CreateLeagueDto.builder().apiFootballId("existing-api-id").name("Test").build();
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
        assertTrue(resultDto.getUpdatedAt()
                .isAfter(existingLeague.getUpdatedAt().minusSeconds(1).atOffset(ZoneOffset.UTC)));

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
        League mockLeague = createMockLeague(UUID.randomUUID().toString(), apiLeagueId, "To Delete",
                Collections.emptyList());
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
