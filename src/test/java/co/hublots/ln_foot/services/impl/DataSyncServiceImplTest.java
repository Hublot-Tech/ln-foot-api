package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.config.SyncConfigProperties;
import co.hublots.ln_foot.dto.external.*;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.HighlightRepository;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.repositories.TeamRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID; // Added import
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSyncServiceImplTest {

    @Mock private WebClient.Builder webClientBuilderMock;
    @Mock private WebClient webClientMock;
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpecMock;
    @Mock private WebClient.ResponseSpec responseSpecMock;

    @Mock private LeagueRepository leagueRepositoryMock;
    @Mock private TeamRepository teamRepositoryMock;
    @Mock private FixtureRepository fixtureRepositoryMock;
    @Mock private HighlightRepository highlightRepositoryMock;
    @Mock private SyncConfigProperties syncConfigPropertiesMock;

    private DataSyncServiceImpl dataSyncService;

    @Captor ArgumentCaptor<League> leagueCaptor;
    @Captor ArgumentCaptor<Team> teamCaptor;
    @Captor ArgumentCaptor<Fixture> fixtureCaptor;

    private final String MOCK_API_URL = "http://mockapi.com";
    private final String MOCK_API_KEY = "mock_key";
    private final String MOCK_API_HOST = "mock_host";
    private final String MOCK_API_SOURCE_NAME = "MockAPISource";

    @BeforeEach
    void setUp() {
        // Configure WebClient mocks
        when(webClientBuilderMock.baseUrl(anyString())).thenReturn(webClientBuilderMock);
        when(webClientBuilderMock.build()).thenReturn(webClientMock);
        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(any(Function.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.header(anyString(), anyString())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);

        dataSyncService = new DataSyncServiceImpl(
                leagueRepositoryMock, teamRepositoryMock, fixtureRepositoryMock,
                highlightRepositoryMock, syncConfigPropertiesMock, webClientBuilderMock, MOCK_API_URL
        );

        // Set @Value fields
        ReflectionTestUtils.setField(dataSyncService, "externalApiSportsKey", MOCK_API_KEY);
        ReflectionTestUtils.setField(dataSyncService, "externalApiRapidApiHost", MOCK_API_HOST);
        ReflectionTestUtils.setField(dataSyncService, "externalApiSourceName", MOCK_API_SOURCE_NAME);
    }

    private FixtureResponseItemDto createMockFixtureResponseItem(long leagueApiId, String leagueName, String leagueCountry,
                                                                 long homeTeamApiId, String homeTeamName,
                                                                 long awayTeamApiId, String awayTeamName,
                                                                 long fixtureApiId, String statusShort) {
        return FixtureResponseItemDto.builder()
                .league(ExternalLeagueInFixtureDto.builder().leagueApiId(leagueApiId).name(leagueName).country(leagueCountry).season(2023).build())
                .teams(TeamsInFixtureDto.builder()
                        .home(ExternalTeamInFixtureDto.builder().teamApiId(homeTeamApiId).name(homeTeamName).logo("home.png").build())
                        .away(ExternalTeamInFixtureDto.builder().teamApiId(awayTeamApiId).name(awayTeamName).logo("away.png").build())
                        .build())
                .fixture(ExternalFixtureDetailsDto.builder()
                        .fixtureApiId(fixtureApiId)
                        .date(OffsetDateTime.now())
                        .timestamp(OffsetDateTime.now().toEpochSecond())
                        .status(StatusDto.builder().shortStatus(statusShort).longStatus("Status Long").build())
                        .venue(VenueDto.builder().name("Mock Venue").build())
                        .build())
                .goals(GoalsDto.builder().home(1).away(0).build())
                .score(ScoreDto.builder().build()) // simplified
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
        apiItems.add(createMockFixtureResponseItem(1L, "Super League", "Mockland", 10L, "Team A", 11L, "Team B", 100L, "FT"));
        apiItems.add(createMockFixtureResponseItem(2L, "Other League", "Otherland", 20L, "Team C", 21L, "Team D", 200L, "NS"));

        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(apiItems);
        mockApiResponse.setResults(apiItems.size());
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(mockApiResponse));

        // Mock repository save operations
        when(leagueRepositoryMock.save(any(League.class))).thenAnswer(inv -> {
            League l = inv.getArgument(0);
            if(l.getId() == null) l.setId(UUID.randomUUID().toString()); // Simulate ID generation
            return l;
        });
        when(teamRepositoryMock.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            if(t.getId() == null) t.setId(UUID.randomUUID().toString());
            return t;
        });
        when(fixtureRepositoryMock.save(any(Fixture.class))).thenAnswer(inv -> {
            Fixture f = inv.getArgument(0);
            if(f.getId() == null) f.setId(UUID.randomUUID().toString());
            return f;
        });

        // Act
        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>()).block();

        // Assert
        assertNotNull(statusDto);
        assertEquals("SUCCESS", statusDto.getStatus());
        assertEquals(1, statusDto.getItemsProcessed()); // Only one fixture matched the filter

        verify(highlightRepositoryMock).deleteAllInBatch();
        verify(fixtureRepositoryMock).deleteAllInBatch();
        verify(teamRepositoryMock).deleteAllInBatch();
        verify(leagueRepositoryMock).deleteAllInBatch();

        verify(leagueRepositoryMock, times(1)).save(leagueCaptor.capture());
        assertEquals("Super League", leagueCaptor.getValue().getLeagueName());
        assertEquals(MOCK_API_SOURCE_NAME, leagueCaptor.getValue().getApiSource());


        verify(teamRepositoryMock, times(2)).save(teamCaptor.capture()); // Team A and Team B
        List<Team> savedTeams = teamCaptor.getAllValues();
        assertTrue(savedTeams.stream().anyMatch(t -> t.getTeamName().equals("Team A")));
        assertTrue(savedTeams.stream().anyMatch(t -> t.getTeamName().equals("Team B")));
        savedTeams.forEach(t -> assertEquals(MOCK_API_SOURCE_NAME, t.getApiSource()));


        verify(fixtureRepositoryMock, times(1)).save(fixtureCaptor.capture());
        assertEquals(String.valueOf(100L), fixtureCaptor.getValue().getApiFixtureId());
        assertEquals("FT", fixtureCaptor.getValue().getStatus());
        assertEquals(MOCK_API_SOURCE_NAME, fixtureCaptor.getValue().getApiSource());
        assertEquals(leagueCaptor.getValue(), fixtureCaptor.getValue().getLeague()); // Check linked league
    }

    @Test
    void syncMainFixtures_noInterestedLeagues_usesFallback() {
        when(syncConfigPropertiesMock.getInterestedLeagues()).thenReturn(Collections.emptyList());

        List<FixtureResponseItemDto> apiItems = new ArrayList<>();
        for(int i=0; i<15; i++) { // Create 15 items
            apiItems.add(createMockFixtureResponseItem((long)i, "League "+i, "Country "+i, (long)(i*10), "Team Home"+i, (long)(i*10+1), "Team Away"+i, (long)(i*100), "NS"));
        }
        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(apiItems);
        mockApiResponse.setResults(apiItems.size());
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(mockApiResponse));

        // Mocks for save to avoid NullPointer on returned entities if ID not set
        when(leagueRepositoryMock.save(any(League.class))).thenAnswer(inv -> inv.getArgument(0));
        when(teamRepositoryMock.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fixtureRepositoryMock.save(any(Fixture.class))).thenAnswer(inv -> inv.getArgument(0));


        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>()).block();

        // Assert
        assertNotNull(statusDto);
        assertEquals("SUCCESS", statusDto.getStatus());
        assertEquals(10, statusDto.getItemsProcessed()); // Fallback processes 10

        // Fallback is 10 items
        verify(fixtureRepositoryMock, times(10)).save(any(Fixture.class));
        // Each fixture has 1 league, 2 teams. If all are unique, 10 leagues, 20 teams.
        // But computeIfAbsent in service ensures unique saves for league/team.
        // For 10 unique fixtures, we'd expect at most 10 unique leagues and 20 unique teams.
        verify(leagueRepositoryMock, atMost(10)).save(any(League.class));
        verify(teamRepositoryMock, atMost(20)).save(any(Team.class));
    }

    @Test
    void syncMainFixtures_apiError_logsAndReturns() {
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class)))
            .thenReturn(Mono.error(new WebClientResponseException("API Error", 500, "Internal Server Error", null, null, null)));

        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>()).block();

        // Assert
        assertNotNull(statusDto);
        assertEquals("ERROR", statusDto.getStatus());
        assertTrue(statusDto.getMessage().contains("API Error: 500"));

        // Verify no DB interactions after API error
        verify(highlightRepositoryMock, never()).deleteAllInBatch();
        verify(fixtureRepositoryMock, never()).deleteAllInBatch();
        // ... and so on for other repos and save methods
        verify(leagueRepositoryMock, never()).save(any(League.class));
    }

    @Test
    void syncMainFixtures_emptyApiResponseList_clearsDbButSavesNothingNew() {
        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(Collections.emptyList()); // Empty list
        mockApiResponse.setResults(0);
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(mockApiResponse));

        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>()).block();

        // Assert
        assertNotNull(statusDto);
        assertEquals("NO_DATA", statusDto.getStatus()); // Or SUCCESS with 0 items, depends on impl. Current impl returns NO_DATA.
        assertEquals(0, statusDto.getItemsProcessed());

        verify(highlightRepositoryMock).deleteAllInBatch();
        verify(fixtureRepositoryMock).deleteAllInBatch();
        verify(teamRepositoryMock).deleteAllInBatch();
        verify(leagueRepositoryMock).deleteAllInBatch();

        verify(leagueRepositoryMock, never()).save(any(League.class));
        verify(teamRepositoryMock, never()).save(any(Team.class));
        verify(fixtureRepositoryMock, never()).save(any(Fixture.class));
    }

    @Test
    void syncMainFixtures_nullApiResponse_clearsDbButSavesNothingNew() {
        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(null); // Null response list
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(mockApiResponse));

        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>()).block();

        // Assert
        assertNotNull(statusDto);
        assertEquals("NO_DATA", statusDto.getStatus());
        assertEquals(0, statusDto.getItemsProcessed());

        verify(highlightRepositoryMock).deleteAllInBatch();
        // ... other deleteAllInBatch verify ...
        verify(fixtureRepositoryMock, never()).save(any(Fixture.class));
    }

    @Test
    void syncMainFixtures_whenDbClearFails_returnsErrorStatusDtoAndLogsError() {
        // Arrange
        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(List.of(createMockFixtureResponseItem(1L, "L1", "C1", 10L, "T1", 11L, "T2", 100L, "FT")));
        mockApiResponse.setResults(1);
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(mockApiResponse));

        // Mock one of the deleteAllInBatch methods to throw an exception
        doThrow(new RuntimeException("DB clear error")).when(highlightRepositoryMock).deleteAllInBatch();

        // Act
        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>()).block();

        // Assert
        assertNotNull(statusDto);
        assertEquals("ERROR", statusDto.getStatus());
        assertTrue(statusDto.getMessage().contains("Error clearing existing data: DB clear error"));

        verify(highlightRepositoryMock).deleteAllInBatch(); // This one was called and threw
        verify(fixtureRepositoryMock, never()).deleteAllInBatch(); // Subsequent clear should not be called
        verify(leagueRepositoryMock, never()).save(any(League.class)); // No saves should happen
    }

    @Test
    void syncMainFixtures_whenLeagueSaveFails_returnsErrorStatusDtoAndLogsError() {
        // Arrange
        when(syncConfigPropertiesMock.getInterestedLeagues()).thenReturn(Collections.emptyList()); // Use fallback to simplify fixture data
        List<FixtureResponseItemDto> apiItems = new ArrayList<>();
        apiItems.add(createMockFixtureResponseItem(1L, "League Save Fail", "CountrySF", 10L, "Team SFH", 11L, "Team SFA", 100L, "NS"));
        RapidApiFootballResponseDto<FixtureResponseItemDto> mockApiResponse = new RapidApiFootballResponseDto<>();
        mockApiResponse.setResponse(apiItems);
        mockApiResponse.setResults(1); // Service will take first (up to 10) due to empty interestedLeagues

        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(mockApiResponse));

        doNothing().when(highlightRepositoryMock).deleteAllInBatch();
        doNothing().when(fixtureRepositoryMock).deleteAllInBatch();
        doNothing().when(teamRepositoryMock).deleteAllInBatch();
        doNothing().when(leagueRepositoryMock).deleteAllInBatch();

        when(leagueRepositoryMock.save(any(League.class))).thenThrow(new RuntimeException("League save error"));

        // Act
        SyncStatusDto statusDto = dataSyncService.syncMainFixtures(new HashMap<>()).block();

        // Assert
        assertNotNull(statusDto);
        assertEquals("ERROR", statusDto.getStatus());
        assertTrue(statusDto.getMessage().contains("Error during DB processing: League save error"));

        verify(leagueRepositoryMock).deleteAllInBatch(); // Clears should have happened
        verify(leagueRepositoryMock).save(any(League.class)); // Save was attempted
        verify(teamRepositoryMock, never()).save(any(Team.class)); // Subsequent saves should not happen
        verify(fixtureRepositoryMock, never()).save(any(Fixture.class));
    }


    // Test deprecated methods to ensure they call syncMainFixtures
    @Test
    void oldSyncLeagues_callsSyncMainFixtures() {
        // Need to spy on the service to verify a call to its own public method
        DataSyncServiceImpl spiedService = spy(dataSyncService);
        doNothing().when(spiedService).syncMainFixtures(anyMap()); // Prevent actual execution

        spiedService.syncLeagues("soccer", "england");

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(spiedService).syncMainFixtures(captor.capture());
        assertTrue(captor.getValue().containsKey("date")); // Default param
    }

    @Test
    void oldSyncTeamsByLeague_callsSyncMainFixtures() {
        DataSyncServiceImpl spiedService = spy(dataSyncService);
        doNothing().when(spiedService).syncMainFixtures(anyMap());

        spiedService.syncTeamsByLeague("league123");

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(spiedService).syncMainFixtures(captor.capture());
        assertEquals("league123", captor.getValue().get("league"));
        assertTrue(captor.getValue().containsKey("season"));
    }

    @Test
    void oldSyncFixturesByLeague_callsSyncMainFixtures() {
        DataSyncServiceImpl spiedService = spy(dataSyncService);
        doNothing().when(spiedService).syncMainFixtures(anyMap());

        spiedService.syncFixturesByLeague("league456", "2024");

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(spiedService).syncMainFixtures(captor.capture());
        assertEquals("league456", captor.getValue().get("league"));
        assertEquals("2024", captor.getValue().get("season"));
    }
}
