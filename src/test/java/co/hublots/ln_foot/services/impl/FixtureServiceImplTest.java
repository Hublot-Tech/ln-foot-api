package co.hublots.ln_foot.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.repositories.TeamRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class FixtureServiceImplTest {

    @Mock
    private FixtureRepository fixtureRepository;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private TeamRepository teamRepository;

    private FixtureServiceImpl fixtureService;

    @BeforeEach
    void setUp() {
        fixtureService = new FixtureServiceImpl(fixtureRepository, leagueRepository, teamRepository);
    }

    private League createMockLeague(String internalId, String apiLeagueId, String name) {
        return League.builder().id(internalId).apiLeagueId(apiLeagueId).leagueName(name).build();
    }

    private Team createMockTeam(String internalId, String apiTeamId, String name) {
        return Team.builder().id(internalId).apiTeamId(apiTeamId).teamName(name).logoUrl("logo.png").build();
    }

    private Fixture createMockFixture(String apiFixtureId, League league, Team team1, Team team2,
            OffsetDateTime matchTime) {
        return Fixture.builder()
                .id(UUID.randomUUID().toString()) // Internal UUID
                .apiFixtureId(apiFixtureId)
                .league(league)
                .team1(team1)
                .team2(team2)
                .matchDatetime(matchTime)
                .status("NS")
                .round("Test Round")
                .venueName("Test Venue")
                .goalsTeam1(0)
                .goalsTeam2(0)
                .apiSource("test-source")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listFixtures_byLeagueApiId_returnsPagedDtos() { // Renamed and updated
        // Arrange
        String leagueApiId = "L1_API";
        Pageable pageable = PageRequest.of(0, 10);
        // League mockLeague = createMockLeague(UUID.randomUUID().toString(),
        // leagueApiId, "Mock League"); // Not directly used by service if repo handles
        // league ID
        Team teamA = createMockTeam(UUID.randomUUID().toString(), "TA_API", "Team A");
        Team teamB = createMockTeam(UUID.randomUUID().toString(), "TB_API", "Team B");
        // Need to associate the mockLeague with the fixture for the DTO mapping to get
        // leagueApiId
        League mockLeagueForFixture = createMockLeague(UUID.randomUUID().toString(), leagueApiId, "Mock League");
        Fixture mockFixture = createMockFixture("FX1_API", mockLeagueForFixture, teamA, teamB, OffsetDateTime.now());

        when(fixtureRepository.findByLeagueApiLeagueId(leagueApiId, pageable))
                .thenReturn(new PageImpl<>(List.of(mockFixture), pageable, 1));

        // Act
        Page<FixtureDto> result = fixtureService.listFixtures(leagueApiId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().isEmpty());
        assertEquals("FX1_API", result.getContent().get(0).getId());
        assertEquals(leagueApiId, result.getContent().get(0).getLeagueId()); // Check DTO mapping
        verify(fixtureRepository).findByLeagueApiLeagueId(leagueApiId, pageable);
    }

    @Test
    void listFixtures_leagueApiIdProvidedButNoFixtures_returnsEmptyPage() { // Renamed
        String leagueApiId = "LEAGUE_WITH_NO_FIXTURES";
        Pageable pageable = PageRequest.of(0, 10);
        when(fixtureRepository.findByLeagueApiLeagueId(leagueApiId, pageable)).thenReturn(Page.empty(pageable));

        // Act
        Page<FixtureDto> result = fixtureService.listFixtures(leagueApiId, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(fixtureRepository).findByLeagueApiLeagueId(leagueApiId, pageable);
    }

    @Test
    void listFixtures_noLeagueId_returnsAllFixturesPaged() { // Renamed
        Pageable pageable = PageRequest.of(0, 10);
        Team teamA = createMockTeam(UUID.randomUUID().toString(), "TA_API", "Team A");
        Team teamB = createMockTeam(UUID.randomUUID().toString(), "TB_API", "Team B");
        League mockLeague = createMockLeague(UUID.randomUUID().toString(), "L1_API", "Mock League");
        Fixture mockFixture = createMockFixture("FX1_API", mockLeague, teamA, teamB, OffsetDateTime.now());

        when(fixtureRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(mockFixture), pageable, 1));

        // Act
        Page<FixtureDto> result = fixtureService.listFixtures(null, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().isEmpty());
        assertEquals("FX1_API", result.getContent().get(0).getId());
        verify(fixtureRepository).findAll(pageable);
    }

    @Test
    void findFixtureById_whenFound_returnsOptionalDto() { // Param is apiFixtureId
        // Arrange
        String apiFixtureId = "fixture-api-123";
        League mockLeague = createMockLeague(UUID.randomUUID().toString(), "L_API", "League");
        Team teamA = createMockTeam(UUID.randomUUID().toString(), "TA_API", "Team A");
        Team teamB = createMockTeam(UUID.randomUUID().toString(), "TB_API", "Team B");
        Fixture mockFixture = createMockFixture(apiFixtureId, mockLeague, teamA, teamB, OffsetDateTime.now());
        when(fixtureRepository.findByApiFixtureId(apiFixtureId)).thenReturn(Optional.of(mockFixture));

        // Act
        Optional<FixtureDto> result = fixtureService.findFixtureById(apiFixtureId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(apiFixtureId, result.get().getId());
        assertEquals(teamA.getApiTeamId(), result.get().getHomeTeam().getId());
        verify(fixtureRepository).findByApiFixtureId(apiFixtureId);
    }

    @Test
    void getUpcomingFixtures_noLeagueId_returnsDtos() {
        OffsetDateTime startDate = OffsetDateTime.now();
        OffsetDateTime endDate = startDate.plusDays(7);
        Fixture mockFixture = createMockFixture("up1", null, null, null, startDate.plusDays(1)); // simplified for this
                                                                                                 // test
        when(fixtureRepository.findByMatchDatetimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(mockFixture));

        List<FixtureDto> result = fixtureService.getUpcomingFixtures(7, null);
        assertEquals(1, result.size());
        assertEquals("up1", result.get(0).getId());
    }

    @Test
    void getUpcomingFixtures_withLeagueId_returnsDtos() {
        String leagueApiId = "L1";
        League mockLeague = createMockLeague(UUID.randomUUID().toString(), leagueApiId, "League");
        Fixture mockFixture = createMockFixture("upL1", mockLeague, null, null, OffsetDateTime.now().plusDays(1));

        when(leagueRepository.findByApiLeagueId(leagueApiId)).thenReturn(Optional.of(mockLeague));
        when(fixtureRepository.findByLeague_IdAndMatchDatetimeBetween(eq(mockLeague.getId()), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of(mockFixture));

        List<FixtureDto> result = fixtureService.getUpcomingFixtures(7, leagueApiId);
        assertEquals(1, result.size());
        assertEquals("upL1", result.get(0).getId());
    }

    @Test
    void getFixturesByDate_noLeagueId_returnsDtos() {
        OffsetDateTime date = OffsetDateTime.now();
        Fixture mockFixture = createMockFixture("dateFix1", null, null, null, date);
        when(fixtureRepository.findByMatchDatetimeBetween(eq(date.withHour(0).withMinute(0).toLocalDateTime()),
                eq(date.withHour(0).withMinute(59).toLocalDateTime())))
                .thenReturn(List.of(mockFixture));

        List<FixtureDto> result = fixtureService.getFixturesByDate(date.toLocalDate(), null);
        assertEquals(1, result.size());
    }

    @Test
    void createFixture_savesAndReturnsDto() {
        // Arrange
        CreateFixtureDto createDto = CreateFixtureDto.builder()
                .id("new-fix-api") // This is apiFixtureId
                .leagueId("league-api-for-fix")
                .homeTeamId("home-team-api")
                .awayTeamId("away-team-api")
                .date(OffsetDateTime.now(ZoneOffset.UTC))
                .statusShort("NS")
                .build();

        League mockLeague = createMockLeague(UUID.randomUUID().toString(), createDto.getLeagueId(), "Test League");
        Team mockHomeTeam = createMockTeam(UUID.randomUUID().toString(), createDto.getHomeTeamId(), "Home Team");
        Team mockAwayTeam = createMockTeam(UUID.randomUUID().toString(), createDto.getAwayTeamId(), "Away Team");

        when(fixtureRepository.findByApiFixtureId(createDto.getId())).thenReturn(Optional.empty());
        when(leagueRepository.findByApiLeagueId(createDto.getLeagueId())).thenReturn(Optional.of(mockLeague));
        when(teamRepository.findByApiTeamId(createDto.getHomeTeamId())).thenReturn(Optional.of(mockHomeTeam));
        when(teamRepository.findByApiTeamId(createDto.getAwayTeamId())).thenReturn(Optional.of(mockAwayTeam));

        ArgumentCaptor<Fixture> fixtureCaptor = ArgumentCaptor.forClass(Fixture.class);
        when(fixtureRepository.save(fixtureCaptor.capture())).thenAnswer(invocation -> {
            Fixture savedFix = invocation.getArgument(0);
            savedFix.setId(UUID.randomUUID().toString()); // internal ID
            savedFix.setCreatedAt(LocalDateTime.now());
            savedFix.setUpdatedAt(LocalDateTime.now());
            return savedFix;
        });

        // Act
        FixtureDto resultDto = fixtureService.createFixture(createDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(createDto.getId(), resultDto.getId()); // DTO id is apiFixtureId
        assertEquals(createDto.getLeagueId(), resultDto.getLeagueId());
        assertEquals(createDto.getHomeTeamId(), resultDto.getHomeTeam().getId());

        Fixture captured = fixtureCaptor.getValue();
        assertEquals(createDto.getId(), captured.getApiFixtureId());
        assertEquals(mockLeague, captured.getLeague());
        assertEquals(mockHomeTeam, captured.getTeam1());
        assertEquals(mockAwayTeam, captured.getTeam2());

        verify(fixtureRepository).save(any(Fixture.class));
    }

    @Test
    void createFixture_whenLeagueNotFound_throwsException() {
        CreateFixtureDto createDto = CreateFixtureDto.builder().id("fix-league-fail").leagueId("L_FAIL").homeTeamId("H")
                .awayTeamId("A").build();
        when(fixtureRepository.findByApiFixtureId("fix-league-fail")).thenReturn(Optional.empty());
        when(leagueRepository.findByApiLeagueId("L_FAIL")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fixtureService.createFixture(createDto));
        verify(leagueRepository).findByApiLeagueId("L_FAIL");
        verify(teamRepository, never()).findByApiTeamId(anyString());
        verify(fixtureRepository, never()).save(any(Fixture.class));
    }

    @Test
    void createFixture_whenHomeTeamNotFound_throwsEntityNotFoundException() {
        CreateFixtureDto createDto = CreateFixtureDto.builder()
                .id("fix-home-team-fail")
                .leagueId("L_OK")
                .homeTeamId("HT_FAIL")
                .awayTeamId("AT_OK")
                .build();
        League mockLeague = createMockLeague(UUID.randomUUID().toString(), "L_OK", "OK League");

        when(fixtureRepository.findByApiFixtureId("fix-home-team-fail")).thenReturn(Optional.empty());
        when(leagueRepository.findByApiLeagueId("L_OK")).thenReturn(Optional.of(mockLeague));
        when(teamRepository.findByApiTeamId("HT_FAIL")).thenReturn(Optional.empty());
        // Optional: Mock away team if needed, but failure should happen before that
        // when(teamRepository.findByApiTeamId("AT_OK")).thenReturn(Optional.of(createMockTeam(UUID.randomUUID().toString(),
        // "AT_OK", "Away Team OK")));

        assertThrows(EntityNotFoundException.class, () -> fixtureService.createFixture(createDto));
        verify(leagueRepository).findByApiLeagueId("L_OK");
        verify(teamRepository).findByApiTeamId("HT_FAIL");
        verify(fixtureRepository, never()).save(any(Fixture.class));
    }

    @Test
    void createFixture_whenAwayTeamNotFound_throwsEntityNotFoundException() {
        CreateFixtureDto createDto = CreateFixtureDto.builder()
                .id("fix-away-team-fail")
                .leagueId("L_OK")
                .homeTeamId("HT_OK")
                .awayTeamId("AT_FAIL")
                .build();
        League mockLeague = createMockLeague(UUID.randomUUID().toString(), "L_OK", "OK League");
        Team mockHomeTeam = createMockTeam(UUID.randomUUID().toString(), "HT_OK", "Home Team OK");

        when(fixtureRepository.findByApiFixtureId("fix-away-team-fail")).thenReturn(Optional.empty());
        when(leagueRepository.findByApiLeagueId("L_OK")).thenReturn(Optional.of(mockLeague));
        when(teamRepository.findByApiTeamId("HT_OK")).thenReturn(Optional.of(mockHomeTeam));
        when(teamRepository.findByApiTeamId("AT_FAIL")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fixtureService.createFixture(createDto));
        verify(leagueRepository).findByApiLeagueId("L_OK");
        verify(teamRepository).findByApiTeamId("HT_OK");
        verify(teamRepository).findByApiTeamId("AT_FAIL");
        verify(fixtureRepository, never()).save(any(Fixture.class));
    }

    @Test
    void updateFixture_whenFound_updatesAndReturnsDto() { // Param is apiFixtureId
        // Arrange
        String apiFixtureId = "fix-to-update";
        League mockLeague = createMockLeague(UUID.randomUUID().toString(), "L_API", "League");
        Team teamA = createMockTeam(UUID.randomUUID().toString(), "TA_API", "Team A");
        Team teamB = createMockTeam(UUID.randomUUID().toString(), "TB_API", "Team B");
        Fixture existingFixture = createMockFixture(apiFixtureId, mockLeague, teamA, teamB, OffsetDateTime.now());

        when(fixtureRepository.findByApiFixtureId(apiFixtureId)).thenReturn(Optional.of(existingFixture));

        UpdateFixtureDto updateDto = UpdateFixtureDto.builder().statusShort("FT").goalsHome(2).goalsAway(1).build();

        when(fixtureRepository.save(any(Fixture.class))).thenAnswer(invocation -> {
            Fixture fxToSave = invocation.getArgument(0);
            fxToSave.setUpdatedAt(LocalDateTime.now());
            return fxToSave;
        });

        // Act
        FixtureDto resultDto = fixtureService.updateFixture(apiFixtureId, updateDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(apiFixtureId, resultDto.getId());
        assertEquals("FT", resultDto.getStatusShortCode());
        assertEquals(2, resultDto.getGoalsHome());
        assertEquals(1, resultDto.getGoalsAway());

        verify(fixtureRepository).findByApiFixtureId(apiFixtureId);
        ArgumentCaptor<Fixture> fixtureCaptor = ArgumentCaptor.forClass(Fixture.class);
        verify(fixtureRepository).save(fixtureCaptor.capture());
        assertEquals("FT", fixtureCaptor.getValue().getStatus());
    }

    @Test
    void updateFixture_whenFixtureNotFound_throwsEntityNotFoundException() {
        String apiFixtureId = "non-existent-fix-id";
        UpdateFixtureDto updateDto = UpdateFixtureDto.builder().statusShort("FT").build();
        when(fixtureRepository.findByApiFixtureId(apiFixtureId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fixtureService.updateFixture(apiFixtureId, updateDto));
        verify(fixtureRepository).findByApiFixtureId(apiFixtureId);
        verify(fixtureRepository, never()).save(any(Fixture.class));
    }

    @Test
    void deleteFixture_whenFound_deletesFixture() { // Param is apiFixtureId
        // Arrange
        String apiFixtureId = "fix-to-delete";
        Fixture mockFixture = createMockFixture(apiFixtureId, null, null, null, OffsetDateTime.now());
        when(fixtureRepository.findByApiFixtureId(apiFixtureId)).thenReturn(Optional.of(mockFixture));
        doNothing().when(fixtureRepository).delete(mockFixture);

        // Act
        assertDoesNotThrow(() -> fixtureService.deleteFixture(apiFixtureId));

        // Assert
        verify(fixtureRepository).findByApiFixtureId(apiFixtureId);
        verify(fixtureRepository).delete(mockFixture);
    }

    @Test
    void deleteFixture_whenFixtureNotFound_throwsEntityNotFoundException() {
        String apiFixtureId = "non-existent-fix-id-del";
        when(fixtureRepository.findByApiFixtureId(apiFixtureId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fixtureService.deleteFixture(apiFixtureId));
        verify(fixtureRepository).findByApiFixtureId(apiFixtureId);
        verify(fixtureRepository, never()).delete(any(Fixture.class));
        verify(fixtureRepository, never()).deleteById(anyString());
    }
}
