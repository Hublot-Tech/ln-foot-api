package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.config.SyncConfigProperties;
import co.hublots.ln_foot.dto.SyncStatusDto;
import co.hublots.ln_foot.dto.external.*;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.Highlight;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.*;
import co.hublots.ln_foot.services.DataSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataSyncServiceImpl implements DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncServiceImpl.class);

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;
    private final HighlightRepository highlightRepository;
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

    @Override
    public void syncLeagues(String sportId, String countryName) {
        log.warn("Old syncLeagues(sportId, countryName) called. It now calls syncMainFixtures with default date params.");
        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("date", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
        log.info("Calling syncMainFixtures with default date param for syncLeagues: {}", defaultParams);
        syncMainFixtures(defaultParams).subscribe(
            syncStatusDto -> {
                log.info("syncMainFixtures (called from syncLeagues with params: {}) completed with status: {}. Message: {}. Items: {}",
                    defaultParams, syncStatusDto.getStatus(), syncStatusDto.getMessage(), syncStatusDto.getItemsProcessed());
            },
            error -> {
                log.error("Error during syncMainFixtures (called from syncLeagues with params: {}): {}",
                    defaultParams, error.getMessage(), error);
            }
        );
    }

    @Override
    public void syncTeamsByLeague(String externalLeagueApiId) {
        log.warn("Old syncTeamsByLeague(externalLeagueApiId) called. It now calls syncMainFixtures.");
        Map<String, String> params = new HashMap<>();
        params.put("league", externalLeagueApiId);
        params.put("season", String.valueOf(LocalDateTime.now().getYear()));
        log.info("Calling syncMainFixtures for syncTeamsByLeague: {}", params);
        syncMainFixtures(params).subscribe(
            syncStatusDto -> {
                log.info("syncMainFixtures (called from syncTeamsByLeague with params: {}) completed with status: {}. Message: {}. Items: {}",
                    params, syncStatusDto.getStatus(), syncStatusDto.getMessage(), syncStatusDto.getItemsProcessed());
            },
            error -> {
                log.error("Error during syncMainFixtures (called from syncTeamsByLeague with params: {}): {}",
                    params, error.getMessage(), error);
            }
        );
    }

    @Override
    public void syncFixturesByLeague(String externalLeagueApiId, String season) {
        log.warn("Old syncFixturesByLeague(externalLeagueApiId, season) called. It now calls syncMainFixtures.");
        Map<String, String> params = new HashMap<>();
        params.put("league", externalLeagueApiId);
        params.put("season", season);
        log.info("Calling syncMainFixtures for syncFixturesByLeague: {}", params);
        syncMainFixtures(params).subscribe(
            syncStatusDto -> {
                log.info("syncMainFixtures (called from syncFixturesByLeague with params: {}) completed with status: {}. Message: {}. Items: {}",
                    params, syncStatusDto.getStatus(), syncStatusDto.getMessage(), syncStatusDto.getItemsProcessed());
            },
            error -> {
                log.error("Error during syncMainFixtures (called from syncFixturesByLeague with params: {}): {}",
                    params, error.getMessage(), error);
            }
        );
    }

    @Override
    @Transactional // Spring's @Transactional might need care with reactive return types.
                  // For blocking operations wrapped in reactive chain, this might apply to the subscription phase.
                  // Consider TransactionalOperator for more fine-grained control if issues arise.
    public Mono<SyncStatusDto> syncMainFixtures(Map<String, String> queryParams) {
        log.info("Starting main fixtures sync with parameters: {}", queryParams);

        return webClient.get()
            .uri(uriBuilder -> {
                uriBuilder.path("/fixtures");
                queryParams.forEach(uriBuilder::queryParam);
                return uriBuilder.build();
            })
            .header("X-RapidAPI-Key", externalApiSportsKey)
            .header("x-rapidapi-host", externalApiRapidApiHost)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<RapidApiFootballResponseDto<FixtureResponseItemDto>>() {})
            .flatMap(apiResponse -> {
                if (apiResponse == null || apiResponse.getResponse() == null || apiResponse.getResponse().isEmpty()) {
                    log.info("No fixtures returned from API for params: {}", queryParams);
                    return Mono.fromRunnable(this::clearAllSyncData)
                               .subscribeOn(Schedulers.boundedElastic()) // Offload blocking DB ops
                               .then(Mono.just(SyncStatusDto.builder().status("NO_DATA").message("No fixtures returned from API.").itemsProcessed(0).build()));
                }

                List<FixtureResponseItemDto> allFixturesFromApi = apiResponse.getResponse();
                List<FixtureResponseItemDto> filteredFixtures = filterFixtures(allFixturesFromApi, queryParams);

                if (filteredFixtures.isEmpty()) {
                    log.info("No fixtures to process after filtering (or fallback).");
                    return Mono.fromRunnable(this::clearAllSyncData)
                               .subscribeOn(Schedulers.boundedElastic())
                               .then(Mono.just(SyncStatusDto.builder().status("SUCCESS").message("No relevant fixtures to process after filtering.").itemsProcessed(0).build()));
                }

                // Offload blocking DB operations (clear and save)
                return Mono.fromCallable(() -> processAndSaveFixtures(filteredFixtures))
                           .subscribeOn(Schedulers.boundedElastic());
            })
            .onErrorResume(WebClientResponseException.class, e -> {
                log.error("Error from external API: {} - {}, Response Body: {}", e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
                return Mono.just(SyncStatusDto.builder().status("ERROR").message("API Error: " + e.getStatusCode()).itemsProcessed(0).build());
            })
            .onErrorResume(Exception.class, e -> {
                log.error("Error during data sync: {}", e.getMessage(), e);
                return Mono.just(SyncStatusDto.builder().status("ERROR").message("Sync Error: " + e.getMessage()).itemsProcessed(0).build());
            });
    }

    private List<FixtureResponseItemDto> filterFixtures(List<FixtureResponseItemDto> allFixturesFromApi, Map<String, String> queryParams) {
        List<FixtureResponseItemDto> filteredFixtures;
        if (syncConfigProperties != null && syncConfigProperties.getInterestedLeagues() != null && !syncConfigProperties.getInterestedLeagues().isEmpty()) {
            filteredFixtures = allFixturesFromApi.stream()
                .filter(item -> item.getLeague() != null && syncConfigProperties.getInterestedLeagues().stream()
                    .anyMatch(interestedLeague ->
                        interestedLeague.getName().equalsIgnoreCase(item.getLeague().getName()) &&
                        interestedLeague.getCountry().equalsIgnoreCase(item.getLeague().getCountry())
                    ))
                .collect(Collectors.toList());
            log.info("Filtered {} fixtures based on {} interested leagues.", filteredFixtures.size(), syncConfigProperties.getInterestedLeagues().size());

            if (filteredFixtures.isEmpty() && !allFixturesFromApi.isEmpty()) {
                log.warn("No fixtures matched interested leagues. Using first 10 fixtures from API response as fallback (total from API: {}). Params: {}", allFixturesFromApi.size(), queryParams);
                filteredFixtures = allFixturesFromApi.stream().limit(10).collect(Collectors.toList());
            }
        } else {
            log.warn("Interested leagues configuration is empty or null. Using first 10 fixtures from API response (total from API: {}). Params: {}", allFixturesFromApi.size(), queryParams);
            filteredFixtures = allFixturesFromApi.stream().limit(10).collect(Collectors.toList());
        }
        return filteredFixtures;
    }

    // This method contains blocking DB operations and should be called within a reactive chain
    // that handles blocking calls appropriately (e.g., subscribeOn(Schedulers.boundedElastic()))
    private SyncStatusDto processAndSaveFixtures(List<FixtureResponseItemDto> fixturesToProcess) {
        log.info("Proceeding to clear and replace data for {} fixtures.", fixturesToProcess.size());
        try {
            clearAllSyncData(); // This is blocking

            Map<Long, League> processedLeaguesThisSync = new HashMap<>();
            Map<Long, Team> processedTeamsThisSync = new HashMap<>();
            int itemsProcessedCount = 0;

            for (FixtureResponseItemDto item : fixturesToProcess) {
                if (item.getLeague() == null || item.getTeams() == null || item.getTeams().getHome() == null || item.getTeams().getAway() == null || item.getFixture() == null) {
                    log.warn("Skipping fixture item due to missing critical data: {}", item);
                    continue;
                }

                ExternalLeagueInFixtureDto extLeague = item.getLeague();
                League league = processedLeaguesThisSync.computeIfAbsent(extLeague.getLeagueApiId(), (apiId) -> {
                    League newLeague = new League();
                    newLeague.setApiLeagueId(String.valueOf(apiId));
                    newLeague.setLeagueName(extLeague.getName());
                    newLeague.setCountry(extLeague.getCountry());
                    newLeague.setLogoUrl(extLeague.getLogo());
                    newLeague.setApiSource(externalApiSourceName);
                    // sportId, tier not in ExternalLeagueInFixtureDto
                    log.info("Saving new league: {} (API ID: {})", newLeague.getLeagueName(), apiId);
                    return leagueRepository.save(newLeague);
                });

                ExternalTeamInFixtureDto extHomeTeam = item.getTeams().getHome();
                Team homeTeam = processedTeamsThisSync.computeIfAbsent(extHomeTeam.getTeamApiId(), (apiId) -> {
                    Team newTeam = new Team();
                    newTeam.setApiTeamId(String.valueOf(apiId));
                    newTeam.setTeamName(extHomeTeam.getName());
                    newTeam.setLogoUrl(extHomeTeam.getLogo());
                    newTeam.setApiSource(externalApiSourceName);
                    // country, foundedYear, stadiumName not in ExternalTeamInFixtureDto
                    log.info("Saving new home team: {} (API ID: {})", newTeam.getTeamName(), apiId);
                    return teamRepository.save(newTeam);
                });

                ExternalTeamInFixtureDto extAwayTeam = item.getTeams().getAway();
                Team awayTeam = processedTeamsThisSync.computeIfAbsent(extAwayTeam.getTeamApiId(), (apiId) -> {
                    Team newTeam = new Team();
                    newTeam.setApiTeamId(String.valueOf(apiId));
                    newTeam.setTeamName(extAwayTeam.getName());
                    newTeam.setLogoUrl(extAwayTeam.getLogo());
                    newTeam.setApiSource(externalApiSourceName);
                    log.info("Saving new away team: {} (API ID: {})", newTeam.getTeamName(), apiId);
                    return teamRepository.save(newTeam);
                });

                ExternalFixtureDetailsDto extFixDetails = item.getFixture();
                Fixture fixture = new Fixture();
                fixture.setApiFixtureId(String.valueOf(extFixDetails.getFixtureApiId()));

                if (extFixDetails.getDate() != null) {
                    fixture.setMatchDatetime(extFixDetails.getDate().toLocalDateTime());
                } else if (extFixDetails.getTimestamp() != 0) {
                    fixture.setMatchDatetime(LocalDateTime.ofInstant(Instant.ofEpochSecond(extFixDetails.getTimestamp()), ZoneOffset.UTC));
                } else {
                    log.warn("Fixture API ID {} has no valid date or timestamp. Setting matchDatetime to null.", extFixDetails.getFixtureApiId());
                    fixture.setMatchDatetime(null); // DB column must be nullable
                }

                if (extFixDetails.getStatus() != null) {
                    fixture.setStatus(extFixDetails.getStatus().getShortStatus());
                } else {
                     log.warn("Fixture API ID {} has no status information. Setting to 'UNKNOWN'.", extFixDetails.getFixtureApiId());
                     fixture.setStatus("UNKNOWN"); // Or some default / skip
                }

                fixture.setRound(extLeague.getName() + " - Season " + extLeague.getSeason()); // Placeholder for round
                if (extFixDetails.getVenue() != null) {
                    fixture.setVenueName(extFixDetails.getVenue().getName());
                }

                if (item.getGoals() != null) {
                    fixture.setGoalsTeam1(item.getGoals().getHome());
                    fixture.setGoalsTeam2(item.getGoals().getAway());
                }

                fixture.setLeague(league);
                fixture.setTeam1(homeTeam);
                fixture.setTeam2(awayTeam);
                fixture.setApiSource(externalApiSourceName);

                fixtureRepository.save(fixture);
                itemsProcessedCount++;
                log.debug("Saved fixture API ID: {}", fixture.getApiFixtureId());
            }
            log.info("Successfully processed and saved {} fixtures.", itemsProcessedCount);
            return SyncStatusDto.builder().status("SUCCESS").message("Successfully synced fixtures.").itemsProcessed(itemsProcessedCount).build();

        } catch (Exception e) {
            log.error("Error during database operations in syncMainFixtures: {}", e.getMessage(), e);
            // This exception will be caught by the reactive chain's onErrorResume
            throw new RuntimeException("Error during DB processing: " + e.getMessage(), e);
        }
    }

    private void clearAllSyncData() {
        log.info("Clearing all synchronized data (Highlights, Fixtures, Teams, Leagues)...");
        try {
            highlightRepository.deleteAllInBatch();
            log.info("Cleared all highlights.");
            fixtureRepository.deleteAllInBatch();
            log.info("Cleared all fixtures.");
            teamRepository.deleteAllInBatch();
            log.info("Cleared all teams.");
            leagueRepository.deleteAllInBatch();
            log.info("Cleared all leagues.");
        } catch (Exception e) {
            log.error("Error during batch deletion phase: {}", e.getMessage(), e);
            // Decide if this should prevent further sync or just be logged.
            // If this throws, the transaction should roll back.
            throw new RuntimeException("Error clearing existing data: " + e.getMessage(), e);
        }
    }
}
