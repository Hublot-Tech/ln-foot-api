package co.hublots.ln_foot.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import co.hublots.ln_foot.config.SyncConfigProperties;
import co.hublots.ln_foot.dto.SyncStatusDto;
import co.hublots.ln_foot.dto.SyncStatusDto.SyncStatus;
import co.hublots.ln_foot.dto.external.ExternalFixtureDetailsDto;
import co.hublots.ln_foot.dto.external.ExternalLeagueInFixtureDto;
import co.hublots.ln_foot.dto.external.ExternalTeamInFixtureDto;
import co.hublots.ln_foot.dto.external.FixtureResponseItemDto;
import co.hublots.ln_foot.dto.external.GoalsDto;
import co.hublots.ln_foot.dto.external.RapidApiFootballResponseDto;
import co.hublots.ln_foot.dto.external.ScoreDto;
import co.hublots.ln_foot.dto.external.StatusDto;
import co.hublots.ln_foot.dto.external.TeamsInFixtureDto;
import co.hublots.ln_foot.dto.external.VenueDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.repositories.TeamRepository;

@ExtendWith(MockitoExtension.class)
class DataSyncServiceImplTest {

    @Mock
    private LeagueRepository leagueRepositoryMock;
    @Mock
    private TeamRepository teamRepositoryMock;
    @Mock
    private FixtureRepository fixtureRepositoryMock;
    @Mock
    private SyncConfigProperties syncConfigPropertiesMock;
    @Mock
    private RestTemplate restTemplateMock;

    @Captor
    ArgumentCaptor<League> leagueCaptor;
    @Captor
    ArgumentCaptor<Team> teamCaptor;
    @Captor
    ArgumentCaptor<List<Fixture>> fixtureListCaptor;

    private DataSyncServiceImpl dataSyncService;

    private final String MOCK_BASE_URL = "http://mockapi.com";
    private final String MOCK_API_KEY = "mock_key";
    private final String MOCK_API_HOST = "mock_host";

    @BeforeEach
    void setUp() {
        dataSyncService = new DataSyncServiceImpl(
                leagueRepositoryMock,
                teamRepositoryMock,
                fixtureRepositoryMock,
                syncConfigPropertiesMock,
                restTemplateMock);

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(dataSyncService, "baseUrl", MOCK_BASE_URL);
        ReflectionTestUtils.setField(dataSyncService, "externalApiSportsKey", MOCK_API_KEY);
        ReflectionTestUtils.setField(dataSyncService, "externalApiRapidApiHost", MOCK_API_HOST);
    }

    private FixtureResponseItemDto createMockFixtureResponseItem(long leagueApiId, String leagueName,
            String leagueCountry,
            long homeTeamApiId, String homeTeamName,
            long awayTeamApiId, String awayTeamName,
            long fixtureApiId, String statusShort) {
        return FixtureResponseItemDto.builder()
                .league(ExternalLeagueInFixtureDto.builder()
                        .leagueApiId(leagueApiId)
                        .name(leagueName)
                        .country(leagueCountry)
                        .season(2023)
                        .build())
                .teams(TeamsInFixtureDto.builder()
                        .home(ExternalTeamInFixtureDto.builder()
                                .teamApiId(homeTeamApiId)
                                .name(homeTeamName)
                                .logo("home.png")
                                .build())
                        .away(ExternalTeamInFixtureDto.builder()
                                .teamApiId(awayTeamApiId)
                                .name(awayTeamName)
                                .logo("away.png")
                                .build())
                        .build())
                .fixture(ExternalFixtureDetailsDto.builder()
                        .fixtureApiId(fixtureApiId)
                        .date(OffsetDateTime.now())
                        .timestamp(OffsetDateTime.now().toEpochSecond())
                        .status(StatusDto.builder()
                                .shortStatus(statusShort)
                                .longStatus("Full Time")
                                .build())
                        .venue(VenueDto.builder()
                                .name("Mock Venue")
                                .build())
                        .build())
                .goals(GoalsDto.builder().home(1).away(0).build())
                .score(ScoreDto.builder().build())
                .build();
    }

    @Test
    void syncMainFixtures_successfulSync_withFiltering_newEntities() {
        // Arrange
        SyncConfigProperties.InterestedLeague interestedLeague = new SyncConfigProperties.InterestedLeague();
        interestedLeague.setName("Super League");
        interestedLeague.setCountry("Mockland");
        when(syncConfigPropertiesMock.getInterestedLeagues()).thenReturn(List.of(interestedLeague));

        List<FixtureResponseItemDto> apiItems = new ArrayList<>();
        apiItems.add(createMockFixtureResponseItem(1L, "Super League", "Mockland", 10L, "Team A", 11L, "Team B", 100L,
                "FT"));
        apiItems.add(createMockFixtureResponseItem(2L, "Other League", "Otherland", 20L, "Team C", 21L, "Team D", 200L,
                "NS"));

        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(apiItems);
        mockApiResponse.setResults(apiItems.size());

        ResponseEntity<RapidApiFootballResponseDto<FixtureResponseItemDto>> responseEntity = new ResponseEntity<>(
                mockApiResponse, org.springframework.http.HttpStatus.OK);

        when(restTemplateMock.exchange(
                any(java.net.URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers
                        .<ParameterizedTypeReference<RapidApiFootballResponseDto<FixtureResponseItemDto>>>any()))
                .thenReturn(responseEntity);

        // Mock repository save operations
        when(leagueRepositoryMock.save(any(League.class))).thenAnswer(inv -> {
            League l = inv.getArgument(0);
            if (l.getId() == null)
                l.setId(UUID.randomUUID().toString());
            return l;
        });
        when(teamRepositoryMock.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            if (t.getId() == null)
                t.setId(UUID.randomUUID().toString());
            return t;
        });
        when(fixtureRepositoryMock.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>());

        // Assert
        assertNotNull(statusDto);
        assertEquals(SyncStatus.SUCCESS, statusDto.getStatus());
        assertEquals(1, statusDto.getItemsProcessed()); // Only one fixture matched the filter

        verify(fixtureRepositoryMock).deleteAllInBatch(any());
        verify(teamRepositoryMock).deleteAllInBatch(any());
        verify(leagueRepositoryMock).deleteAllInBatch(any());

        verify(leagueRepositoryMock, times(1)).save(leagueCaptor.capture());
        assertEquals("Super League", leagueCaptor.getValue().getLeagueName());

        verify(teamRepositoryMock, times(2)).save(teamCaptor.capture());
        List<Team> savedTeams = teamCaptor.getAllValues();
        assertTrue(savedTeams.stream().anyMatch(t -> t.getTeamName().equals("Team A")));
        assertTrue(savedTeams.stream().anyMatch(t -> t.getTeamName().equals("Team B")));

        verify(fixtureRepositoryMock).saveAll(fixtureListCaptor.capture());
        List<Fixture> savedFixtures = fixtureListCaptor.getValue();
        assertEquals(1, savedFixtures.size());
        assertEquals(String.valueOf(100L), savedFixtures.get(0).getApiFixtureId());
        assertEquals("FT", savedFixtures.get(0).getStatus());
    }

    @Test
    void syncMainFixtures_noInterestedLeagues_usesFallback() {
        when(syncConfigPropertiesMock.getInterestedLeagues()).thenReturn(Collections.emptyList());

        List<FixtureResponseItemDto> apiItems = new ArrayList<>();
        for (int i = 0; i < 15; i++) { // Create 15 items
            apiItems.add(createMockFixtureResponseItem((long) i, "League " + i, "Country " + i, (long) (i * 10),
                    "Team Home" + i, (long) (i * 10 + 1), "Team Away" + i, (long) (i * 100), "NS"));
        }
        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(apiItems);
        mockApiResponse.setResults(apiItems.size());

        ResponseEntity<RapidApiFootballResponseDto<FixtureResponseItemDto>> responseEntity = new ResponseEntity<>(
                mockApiResponse, org.springframework.http.HttpStatus.OK);

        when(restTemplateMock.exchange(
                any(java.net.URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers
                        .<ParameterizedTypeReference<RapidApiFootballResponseDto<FixtureResponseItemDto>>>any()))
                .thenReturn(responseEntity);

        when(leagueRepositoryMock.save(any(League.class))).thenAnswer(inv -> inv.getArgument(0));
        when(teamRepositoryMock.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fixtureRepositoryMock.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>());

        // Assert
        assertNotNull(statusDto);
        assertEquals(SyncStatus.SUCCESS, statusDto.getStatus());
        assertEquals(10, statusDto.getItemsProcessed()); // Fallback processes 10

        verify(fixtureRepositoryMock).saveAll(anyList());
        // Each fixture has 1 league, 2 teams. computeIfAbsent ensures unique saves
        verify(leagueRepositoryMock, atMost(10)).save(any(League.class));
        verify(teamRepositoryMock, atMost(20)).save(any(Team.class));
    }

    @Test
    void syncMainFixtures_apiError_logsAndReturns() {
        when(restTemplateMock.exchange(
                any(java.net.URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<FixtureResponseItemDto>>any()))
                .thenThrow(new RestClientException("API Error"));

        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>());

        // Assert
        assertNotNull(statusDto);
        assertEquals(SyncStatus.ERROR, statusDto.getStatus());
        assertTrue(statusDto.getMessage().contains("API Error"));

        // Verify no DB interactions after API error
        verify(fixtureRepositoryMock, never()).deleteAllInBatch();
        verify(leagueRepositoryMock, never()).save(any(League.class));
    }

    @Test
    void syncMainFixtures_emptyApiResponseList_SavesNothingNew() {
        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(Collections.emptyList());
        mockApiResponse.setResults(0);

        ResponseEntity<RapidApiFootballResponseDto<FixtureResponseItemDto>> responseEntity = new ResponseEntity<>(
                mockApiResponse, org.springframework.http.HttpStatus.OK);

        when(restTemplateMock.exchange(
                any(java.net.URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers
                        .<ParameterizedTypeReference<RapidApiFootballResponseDto<FixtureResponseItemDto>>>any()))
                .thenReturn(responseEntity);

        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>());

        // Assert
        assertNotNull(statusDto);
        assertEquals(SyncStatus.NO_DATA, statusDto.getStatus());
        assertEquals(0, statusDto.getItemsProcessed());

        verify(leagueRepositoryMock, never()).save(any(League.class));
        verify(teamRepositoryMock, never()).save(any(Team.class));
        verify(fixtureRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    void syncMainFixtures_nullApiResponse_SavesNothingNew() {
        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(null);

        ResponseEntity<RapidApiFootballResponseDto<FixtureResponseItemDto>> responseEntity = new ResponseEntity<>(
                mockApiResponse, org.springframework.http.HttpStatus.OK);

        when(restTemplateMock.exchange(
                any(java.net.URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers
                        .<ParameterizedTypeReference<RapidApiFootballResponseDto<FixtureResponseItemDto>>>any()))
                .thenReturn(responseEntity);

        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>());

        // Assert
        assertNotNull(statusDto);
        assertEquals(SyncStatus.NO_DATA, statusDto.getStatus());
        assertEquals(0, statusDto.getItemsProcessed());

        verify(fixtureRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    void syncMainFixtures_whenLeagueSaveFails_returnsErrorStatusDtoAndLogsError() {
        // Arrange
        when(syncConfigPropertiesMock.getInterestedLeagues()).thenReturn(Collections.emptyList());

        List<FixtureResponseItemDto> apiItems = new ArrayList<>();
        apiItems.add(createMockFixtureResponseItem(1L, "League Save Fail", "CountrySF", 10L, "Team SFH", 11L,
                "Team SFA", 100L, "NS"));

        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(apiItems);
        mockApiResponse.setResults(1);

        ResponseEntity<RapidApiFootballResponseDto<FixtureResponseItemDto>> responseEntity = new ResponseEntity<>(
                mockApiResponse, org.springframework.http.HttpStatus.OK);

        when(restTemplateMock.exchange(
                any(java.net.URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers
                        .<ParameterizedTypeReference<RapidApiFootballResponseDto<FixtureResponseItemDto>>>any()))
                .thenReturn(responseEntity);

        when(leagueRepositoryMock.save(any(League.class))).thenThrow(new RuntimeException("League save error"));

        // Act
        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>());

        // Assert
        assertNotNull(statusDto);
        assertEquals(SyncStatus.ERROR, statusDto.getStatus());
        assertTrue(statusDto.getMessage().contains("Sync Error: League save error"));

        verify(leagueRepositoryMock).save(any(League.class));
        verify(teamRepositoryMock, never()).save(any(Team.class));
        verify(fixtureRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    void oldSyncLeagues_callsSyncMainFixtures() {
        DataSyncServiceImpl spiedService = spy(dataSyncService);
        doReturn(SyncStatusDto.builder().status(SyncStatus.SUCCESS).build()).when(spiedService).syncMainFixtures(anyMap());

        spiedService.syncLeagues("soccer", "england");

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.captor();
        verify(spiedService).syncMainFixtures(captor.capture());
        assertTrue(captor.getValue().containsKey("date"));
    }

    @Test
    void oldSyncTeamsByLeague_callsSyncMainFixtures() {
        DataSyncServiceImpl spiedService = spy(dataSyncService);
        doReturn(SyncStatusDto.builder().status(SyncStatus.SUCCESS).build()).when(spiedService).syncMainFixtures(anyMap());

        spiedService.syncTeamsByLeague("league123");

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.captor();
        verify(spiedService).syncMainFixtures(captor.capture());
        assertEquals("league123", captor.getValue().get("league"));
        assertTrue(captor.getValue().containsKey("season"));
    }

    @Test
    void oldSyncFixturesByLeague_callsSyncMainFixtures() {
        DataSyncServiceImpl spiedService = spy(dataSyncService);
        doReturn(SyncStatusDto.builder().status(SyncStatus.SUCCESS).build()).when(spiedService).syncMainFixtures(anyMap());

        spiedService.syncFixturesByLeague("league456", "2024");

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.captor();
        verify(spiedService).syncMainFixtures(captor.capture());
        assertEquals("league456", captor.getValue().get("league"));
        assertEquals("2024", captor.getValue().get("season"));
    }
}