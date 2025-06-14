package co.hublots.ln_foot.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import co.hublots.ln_foot.config.SyncConfigProperties;
import co.hublots.ln_foot.dto.SyncStatusDto;
import co.hublots.ln_foot.dto.external.ExternalLeagueInFixtureDto;
import co.hublots.ln_foot.dto.external.ExternalTeamInFixtureDto;
import co.hublots.ln_foot.dto.external.FixtureResponseItemDto;
import co.hublots.ln_foot.dto.external.RapidApiFootballResponseDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.HighlightRepository;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.repositories.TeamRepository;
import co.hublots.ln_foot.services.DataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncServiceImpl implements DataSyncService {


    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;
    private final HighlightRepository highlightRepository;
    private final SyncConfigProperties syncConfigProperties;
    private final RestTemplate restTemplate;

    @Value("${external.api.sportsUrl}")
    private final String baseUrl;

    @Value("${external.api.sportsKey}")
    private String externalApiSportsKey;

    @Value("${external.api.rapidApiHost}")
    private String externalApiRapidApiHost;

    @Override
    public void syncLeagues(String sportId, String countryName) {
        log.warn(
                "Old syncLeagues(sportId, countryName) called. It now calls syncMainFixtures with default date params.");
        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("date", LocalDateTime.now().toLocalDate().toString());
        SyncStatusDto status = syncMainFixtures(defaultParams);
        log.info("syncMainFixtures completed with status: {}", status);
    }

    @Override
    public void syncTeamsByLeague(String externalLeagueApiId) {
        log.warn("Old syncTeamsByLeague called. It now calls syncMainFixtures.");
        Map<String, String> params = new HashMap<>();
        params.put("league", externalLeagueApiId);
        params.put("season", String.valueOf(LocalDateTime.now().getYear()));
        SyncStatusDto status = syncMainFixtures(params);
        log.info("syncMainFixtures completed with status: {}", status);
    }

    @Override
    public void syncFixturesByLeague(String externalLeagueApiId, String season) {
        log.warn("Old syncFixturesByLeague called. It now calls syncMainFixtures.");
        Map<String, String> params = new HashMap<>();
        params.put("league", externalLeagueApiId);
        params.put("season", season);
        SyncStatusDto status = syncMainFixtures(params);
        log.info("syncMainFixtures completed with status: {}", status);
    }

    @Override
    @Transactional
    public SyncStatusDto syncMainFixtures(Map<String, String> queryParams) {
        try {
            String url = baseUrl + "/fixtures";
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
            queryParams.forEach(uriBuilder::queryParam);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", externalApiSportsKey);
            headers.set("x-rapidapi-host", externalApiRapidApiHost);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<RapidApiFootballResponseDto<FixtureResponseItemDto>> response = restTemplate.exchange(
                    uriBuilder.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });

            List<FixtureResponseItemDto> allFixturesFromApi = Optional.ofNullable(response.getBody())
                    .map(RapidApiFootballResponseDto::getResponse)
                    .orElse(Collections.emptyList());

            if (allFixturesFromApi.isEmpty()) {
                log.info("No fixtures returned. Clearing old data.");
                clearAllSyncData();
                return SyncStatusDto.builder().status("NO_DATA").message("No fixtures returned from API.")
                        .itemsProcessed(0).build();
            }

            List<FixtureResponseItemDto> filteredFixtures = filterFixtures(allFixturesFromApi, queryParams);

            if (filteredFixtures.isEmpty()) {
                clearAllSyncData();
                return SyncStatusDto.builder().status("SUCCESS").message("No relevant fixtures to process.")
                        .itemsProcessed(0).build();
            }

            return processAndSaveFixtures(filteredFixtures);

        } catch (RestClientException e) {
            log.error("RestClientException during sync: {}", e.getMessage(), e);
            return SyncStatusDto.builder().status("ERROR").message("API Error: " + e.getMessage()).itemsProcessed(0)
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during sync: {}", e.getMessage(), e);
            return SyncStatusDto.builder().status("ERROR").message("Sync Error: " + e.getMessage()).itemsProcessed(0)
                    .build();
        }
    }

    private List<FixtureResponseItemDto> filterFixtures(List<FixtureResponseItemDto> allFixturesFromApi,
            Map<String, String> queryParams) {
        List<FixtureResponseItemDto> filteredFixtures;

        if (syncConfigProperties != null && syncConfigProperties.getInterestedLeagues() != null
                && !syncConfigProperties.getInterestedLeagues().isEmpty()) {
            filteredFixtures = allFixturesFromApi.stream()
                    .filter(item -> item.getLeague() != null &&
                            syncConfigProperties.getInterestedLeagues().stream()
                                    .anyMatch(interestedLeague -> interestedLeague.getName()
                                            .equalsIgnoreCase(item.getLeague().getName()) &&
                                            interestedLeague.getCountry()
                                                    .equalsIgnoreCase(item.getLeague().getCountry())))
                    .collect(Collectors.toList());

            if (filteredFixtures.isEmpty()) {
                log.warn("No fixtures matched interested leagues. Using first 10 fixtures as fallback.");
                filteredFixtures = allFixturesFromApi.stream().limit(10).collect(Collectors.toList());
            }
        } else {
            log.warn("No interested leagues configured. Using first 10 fixtures as fallback.");
            filteredFixtures = allFixturesFromApi.stream().limit(10).collect(Collectors.toList());
        }

        return filteredFixtures;
    }

    private SyncStatusDto processAndSaveFixtures(List<FixtureResponseItemDto> fixturesToProcess) {
        Map<Long, League> processedLeagues = new HashMap<>();
        Map<Long, Team> processedTeams = new HashMap<>();

        int count = 0;
        List<Fixture> fixturesToSave = new ArrayList<Fixture>(fixturesToProcess.size());

        for (FixtureResponseItemDto item : fixturesToProcess) {
            if (item.getFixture() == null || item.getTeams() == null || item.getLeague() == null) {
                log.warn("Skipping incomplete fixture data: {}", item);
                continue; // Skip incomplete fixtures
            }
            // Save League
            ExternalLeagueInFixtureDto extLeague = item.getLeague();
            League league = processedLeagues.computeIfAbsent(extLeague.getLeagueApiId(), id -> {
                League l = new League();
                l.setApiLeagueId(String.valueOf(id));
                l.setLeagueName(extLeague.getName());
                l.setCountry(extLeague.getCountry());
                return leagueRepository.save(l);
            });

            // Save Teams
            ExternalTeamInFixtureDto homeTeamDto = item.getTeams().getHome();
            Team homeTeam = processedTeams.computeIfAbsent(homeTeamDto.getTeamApiId(), id -> {
                Team t = new Team();
                t.setApiTeamId(String.valueOf(id));
                t.setTeamName(homeTeamDto.getName());
                t.setLogoUrl(homeTeamDto.getLogo());
                return teamRepository.save(t);
            });

            ExternalTeamInFixtureDto awayTeamDto = item.getTeams().getAway();
            Team awayTeam = processedTeams.computeIfAbsent(awayTeamDto.getTeamApiId(), id -> {
                Team t = new Team();
                t.setApiTeamId(String.valueOf(id));
                t.setTeamName(awayTeamDto.getName());
                t.setLogoUrl(awayTeamDto.getLogo());
                return teamRepository.save(t);
            });

            // Save Fixture
            Fixture fixture = new Fixture();
            fixture.setApiFixtureId(String.valueOf(item.getFixture().getFixtureApiId()));
            fixture.setLeague(league);
            fixture.setTeam1(homeTeam);
            fixture.setTeam2(awayTeam);
            fixture.setStatus(item.getFixture().getStatus().getLongStatus());
            fixture.setMatchDatetime(item.getFixture().getDate());

            fixturesToSave.add(fixture);
            count++;
        }

        if (fixturesToSave.isEmpty()) {
            log.info("No valid fixtures to save after processing.");
            return SyncStatusDto.builder().status("NO_DATA").message("No valid fixtures to save.").itemsProcessed(0)
                    .build();
        }

        clearAllSyncData();
        fixtureRepository.saveAll(fixturesToSave);
        return SyncStatusDto.builder()
                .status("SUCCESS")
                .message("Synced fixtures successfully")
                .itemsProcessed(count)
                .build();
    }

    private void clearAllSyncData() {
        highlightRepository.deleteAllInBatch();
        fixtureRepository.deleteAllInBatch();
        teamRepository.deleteAllInBatch();
        leagueRepository.deleteAllInBatch();
    }
}
