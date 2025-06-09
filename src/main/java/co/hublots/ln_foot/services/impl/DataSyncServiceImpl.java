package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.config.SyncConfigProperties;
import co.hublots.ln_foot.dto.external.*;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.Highlight; // Assuming Highlight is child of Fixture
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.*;
import co.hublots.ln_foot.services.DataSyncService;
import jakarta.persistence.EntityNotFoundException; // Should already be there
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
// @RequiredArgsConstructor // Cannot use with WebClient field initialized in constructor from builder
public class DataSyncServiceImpl implements DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncServiceImpl.class);

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;
    private final HighlightRepository highlightRepository; // Added for clearing
    private final SyncConfigProperties syncConfigProperties;
    private final WebClient webClient;

    @Value("${external.api.sportsKey}")
    private String externalApiSportsKey;

    @Value("${external.api.rapidApiHost}")
    private String externalApiRapidApiHost;

    @Value("${external.api.sourceName}")
    private String externalApiSourceName;

    public DataSyncServiceImpl(LeagueRepository leagueRepository,
                               TeamRepository teamRepository,
                               FixtureRepository fixtureRepository,
                               HighlightRepository highlightRepository,
                               SyncConfigProperties syncConfigProperties,
                               WebClient.Builder webClientBuilder,
                               @Value("${external.api.sportsUrl}") String externalApiSportsUrl) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.fixtureRepository = fixtureRepository;
        this.highlightRepository = highlightRepository;
        this.syncConfigProperties = syncConfigProperties;
        this.webClient = webClientBuilder.baseUrl(externalApiSportsUrl).build();
    }


    // Old syncLeagues, syncTeamsByLeague, syncFixturesByLeague methods are removed as per instruction.
// import co.hublots.ln_foot.services.DataSyncService; // Already in file
import co.hublots.ln_foot.dto.SyncStatusDto; // Added for return type

// ... other imports ...

// @Service
// @RequiredArgsConstructor // Cannot use with WebClient field initialized in constructor from builder
public class DataSyncServiceImpl implements DataSyncService {

    // ... existing fields ...

    // Old syncLeagues, syncTeamsByLeague, syncFixturesByLeague methods are removed as per instruction.
    // New method syncMainFixtures will replace them.

    @Override
    public void syncLeagues(String sportId, String countryName) {
        log.warn("Old syncLeagues(sportId, countryName) called but is deprecated. It now calls syncMainFixtures.");
        Map<String, String> defaultParams = new HashMap<>();
        // Defaulting to current date as /fixtures usually requires date, league, or live status.
        // A general "sync leagues for a country/sport" might need a different API endpoint first
        // to discover league IDs, then call /fixtures per league.
        defaultParams.put("date", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
        // Potentially add sportId or country if the /fixtures endpoint supports it broadly,
        // or this method needs to be smarter (e.g. first fetch league IDs for country/sport).
        log.info("Calling syncMainFixtures with default date param for syncLeagues: {}", defaultParams);
        syncMainFixtures(defaultParams);
    }

    @Override
    public void syncTeamsByLeague(String externalLeagueApiId) {
        log.warn("Old syncTeamsByLeague(externalLeagueApiId) called but is deprecated. It now calls syncMainFixtures.");
        Map<String, String> params = new HashMap<>();
        params.put("league", externalLeagueApiId);
        params.put("season", String.valueOf(LocalDateTime.now().getYear())); // Assume current season
        log.info("Calling syncMainFixtures for syncTeamsByLeague: {}", params);
        syncMainFixtures(params);
    }

    @Override
    public void syncFixturesByLeague(String externalLeagueApiId, String season) {
        log.warn("Old syncFixturesByLeague(externalLeagueApiId, season) called but is deprecated. It now calls syncMainFixtures.");
        Map<String, String> params = new HashMap<>();
        params.put("league", externalLeagueApiId);
        params.put("season", season);
        log.info("Calling syncMainFixtures for syncFixturesByLeague: {}", params);
        syncMainFixtures(params);
    }

    @Override // Added to match interface
    @Transactional
    public SyncStatusDto syncMainFixtures(Map<String, String> queryParams) {
        log.info("Starting main fixtures sync with parameters: {}", queryParams);
        int itemsProcessedCount = 0;

        RapidApiFootballResponseDto<FixtureResponseItemDto> apiResponse;
        try {
            Mono<RapidApiFootballResponseDto<FixtureResponseItemDto>> responseMono = this.webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/fixtures"); // endpoint path
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header("X-RapidAPI-Key", externalApiSportsKey)
                .header("x-rapidapi-host", externalApiRapidApiHost)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RapidApiFootballResponseDto<FixtureResponseItemDto>>() {});

            apiResponse = responseMono.block(); // Using .block() for simplicity in scheduled task context
        } catch (WebClientResponseException e) {
            log.error("Error from external API: {} - {}, Response Body: {}", e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
            return SyncStatusDto.builder().status("ERROR").message("API Error: " + e.getStatusCode()).itemsProcessed(0).build();
        } catch (Exception e) {
            log.error("Error during WebClient call: {}", e.getMessage(), e);
            return SyncStatusDto.builder().status("ERROR").message("WebClient Error: " + e.getMessage()).itemsProcessed(0).build();
        }

        if (apiResponse == null || apiResponse.getResponse() == null || apiResponse.getResponse().isEmpty()) {
            log.info("No fixtures returned from API for params: {}", queryParams);
            // Clear relevant tables even if no new data comes.
            clearAllSyncData();
            return SyncStatusDto.builder().status("NO_DATA").message("No fixtures returned from API.").itemsProcessed(0).build();
        }

        List<FixtureResponseItemDto> allFixturesFromApi = apiResponse.getResponse();
        List<FixtureResponseItemDto> filteredFixtures;

        if (syncConfigProperties != null && syncConfigProperties.getInterestedLeagues() != null && !syncConfigProperties.getInterestedLeagues().isEmpty()) {
            filteredFixtures = allFixturesFromApi.stream()
                .filter(item -> syncConfigProperties.getInterestedLeagues().stream()
                    .anyMatch(interestedLeague ->
                        interestedLeague.getName().equalsIgnoreCase(item.getLeague().getName()) &&
                        interestedLeague.getCountry().equalsIgnoreCase(item.getLeague().getCountry())
                    ))
                .collect(Collectors.toList());
            log.info("Filtered {} fixtures based on {} interested leagues.", filteredFixtures.size(), syncConfigProperties.getInterestedLeagues().size());

            if (filteredFixtures.isEmpty() && !allFixturesFromApi.isEmpty()) {
                log.warn("No fixtures matched interested leagues. Using first 10 fixtures from API response as fallback (total from API: {}).", allFixturesFromApi.size());
                filteredFixtures = allFixturesFromApi.stream().limit(10).collect(Collectors.toList());
            }
        } else {
            log.warn("Interested leagues configuration is empty or null. Using first 10 fixtures from API response (total from API: {}).", allFixturesFromApi.size());
            filteredFixtures = allFixturesFromApi.stream().limit(10).collect(Collectors.toList());
        }


        if (filteredFixtures.isEmpty()) {
            log.info("No fixtures to process after filtering (or fallback).");
            // Clear relevant tables even if no fixtures to process after filtering.
            clearAllSyncData();
            return SyncStatusDto.builder().status("SUCCESS").message("No relevant fixtures to process after filtering.").itemsProcessed(0).build();
        }

        log.info("Proceeding to clear and replace data for {} fixtures.", filteredFixtures.size());
        clearAllSyncData();

        Map<Long, League> processedLeaguesThisSync = new HashMap<>();
        Map<Long, Team> processedTeamsThisSync = new HashMap<>();

        for (FixtureResponseItemDto item : filteredFixtures) {
            ExternalLeagueInFixtureDto extLeague = item.getLeague();
            League league = processedLeaguesThisSync.computeIfAbsent(extLeague.getLeagueApiId(), (apiId) -> {
                League newLeague = new League();
                newLeague.setApiLeagueId(String.valueOf(apiId)); // API ID is long, entity ID is String
                newLeague.setLeagueName(extLeague.getName());
                newLeague.setCountry(extLeague.getCountry());
                newLeague.setLogoUrl(extLeague.getLogo());
                // newLeague.setSportId(); // Not in ExternalLeagueInFixtureDto
                newLeague.setTier(null); // Not in ExternalLeagueInFixtureDto, default or fetch separately
                newLeague.setApiSource(externalApiSourceName);
                log.info("Saving new league: {} (API ID: {})", newLeague.getLeagueName(), apiId);
                return leagueRepository.save(newLeague);
            });

            ExternalTeamInFixtureDto extHomeTeam = item.getTeams().getHome();
            Team homeTeam = processedTeamsThisSync.computeIfAbsent(extHomeTeam.getTeamApiId(), (apiId) -> {
                Team newTeam = new Team();
                newTeam.setApiTeamId(String.valueOf(apiId));
                newTeam.setTeamName(extHomeTeam.getName());
                newTeam.setLogoUrl(extHomeTeam.getLogo());
                // newTeam.setCountry(); // Not in ExternalTeamInFixtureDto
                newTeam.setApiSource(externalApiSourceName);
                log.info("Saving new home team: {} (API ID: {})", newTeam.getTeamName(), apiId);
                return teamRepository.save(newTeam);
            });

            ExternalTeamInFixtureDto extAwayTeam = item.getTeams().getAway();
            Team awayTeam = processedTeamsThisSync.computeIfAbsent(extAwayTeam.getTeamApiId(), (apiId) -> {
                Team newTeam = new Team();
                newTeam.setApiTeamId(String.valueOf(apiId));
                newTeam.setTeamName(extAwayTeam.getName());
                newTeam.setLogoUrl(extAwayTeam.getLogo());
                // newTeam.setCountry();
                newTeam.setApiSource(externalApiSourceName);
                log.info("Saving new away team: {} (API ID: {})", newTeam.getTeamName(), apiId);
                return teamRepository.save(newTeam);
            });

            Fixture fixture = new Fixture();
            ExternalFixtureDetailsDto extFixtureDetails = item.getFixture();
            fixture.setApiFixtureId(String.valueOf(extFixtureDetails.getFixtureApiId()));
            if (extFixtureDetails.getDate() != null) {
                 fixture.setMatchDatetime(extFixtureDetails.getDate().toLocalDateTime());
            } else {
                // Fallback to timestamp if date is null, though API should provide 'date'
                fixture.setMatchDatetime(LocalDateTime.ofEpochSecond(extFixtureDetails.getTimestamp(), 0, ZoneOffset.UTC));
            }
            fixture.setStatus(extFixtureDetails.getStatus().getShortStatus());
            // Assuming 'round' might be part of league name or season, or a specific field if API provides it.
            // For instance, if league.name is "Premier League - Round 10", this needs parsing.
            // Using a combination from league for now as a placeholder.
            fixture.setRound(item.getLeague().getName() + " - Season " + item.getLeague().getSeason());
            fixture.setVenueName(extFixtureDetails.getVenue() != null ? extFixtureDetails.getVenue().getName() : null);

            fixture.setGoalsTeam1(item.getGoals().getHome());
            fixture.setGoalsTeam2(item.getGoals().getAway());

            fixture.setLeague(league);
            fixture.setTeam1(homeTeam);
            fixture.setTeam2(awayTeam);
            fixture.setApiSource(externalApiSourceName);

            fixtureRepository.save(fixture);
            itemsProcessedCount++;
            log.debug("Saved fixture API ID: {}", fixture.getApiFixtureId());
        }
        log.info("Successfully synced {} fixtures.", itemsProcessedCount);
        return SyncStatusDto.builder().status("SUCCESS").message("Successfully synced fixtures.").itemsProcessed(itemsProcessedCount).build();
    }

    private void clearAllSyncData() {
        // Clear Data (Order is important)
        highlightRepository.deleteAllInBatch();
        log.info("Cleared all highlights.");
        fixtureRepository.deleteAllInBatch();
        log.info("Cleared all fixtures.");
        teamRepository.deleteAllInBatch();
        log.info("Cleared all teams.");
        leagueRepository.deleteAllInBatch();
        log.info("Cleared all leagues.");
    }
}
