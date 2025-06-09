package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.external.ExternalFixtureDto;
import co.hublots.ln_foot.dto.external.ExternalLeagueDto;
import co.hublots.ln_foot.dto.external.ExternalTeamDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.models.Team;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.repositories.TeamRepository;
import co.hublots.ln_foot.services.DataSyncService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux; // For WebClient

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataSyncServiceImpl implements DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncServiceImpl.class);

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;
    // private final WebClient.Builder webClientBuilder; // Would use this for real API calls

    @Value("${external.api.baseUrl}")
    private String externalApiBaseUrl;

    @Value("${external.api.key}")
    private String externalApiKey;

    @Value("${external.api.sourceName}")
    private String externalApiSourceName;

    @Override
    @Transactional
    public void syncLeagues(String sportId, String countryName) {
        log.info("Starting league sync for sportId: {}, countryName: {}", sportId, countryName);
        // String apiUrl = externalApiBaseUrl + "/leagues?sport=" + sportId + "&country=" + countryName + "&apiKey=" + externalApiKey;
        // Flux<ExternalLeagueDto> responseFlux = webClientBuilder.build().get().uri(apiUrl).retrieve().bodyToFlux(ExternalLeagueDto.class);
        // List<ExternalLeagueDto> externalLeagues = responseFlux.collectList().block(); // Simplified blocking call for example

        // Hardcoded response for this subtask
        List<ExternalLeagueDto> externalLeagues = List.of(
                ExternalLeagueDto.builder().apiLeagueId("ext-league-1").name("Premier Mock League").country("Mockland").logoUrl("http://logo.url/pml.png").sportId("soccer").tier(1).build(),
                ExternalLeagueDto.builder().apiLeagueId("ext-league-2").name("Second Mock Division").country("Mockland").logoUrl("http://logo.url/smd.png").sportId("soccer").tier(2).build()
        );

        if (externalLeagues == null || externalLeagues.isEmpty()) {
            log.info("No leagues found from external API for sportId: {}, countryName: {}", sportId, countryName);
            return;
        }

        for (ExternalLeagueDto extLeague : externalLeagues) {
            Optional<League> existingLeagueOpt = leagueRepository.findByApiLeagueIdAndApiSource(extLeague.getApiLeagueId(), externalApiSourceName);
            League league;
            if (existingLeagueOpt.isPresent()) {
                league = existingLeagueOpt.get();
                log.info("Updating existing league: {} (API ID: {})", league.getLeagueName(), extLeague.getApiLeagueId());
            } else {
                league = new League();
                league.setApiLeagueId(extLeague.getApiLeagueId());
                log.info("Creating new league with API ID: {}", extLeague.getApiLeagueId());
            }
            league.setLeagueName(extLeague.getName());
            league.setCountry(extLeague.getCountry());
            league.setLogoUrl(extLeague.getLogoUrl());
            league.setSportId(extLeague.getSportId());
            league.setTier(extLeague.getTier());
            league.setApiSource(externalApiSourceName);
            leagueRepository.save(league);
        }
        log.info("Finished league sync. Processed {} leagues.", externalLeagues.size());
    }

    @Override
    @Transactional
    public void syncTeamsByLeague(String externalLeagueApiId) {
        log.info("Starting team sync for externalLeagueApiId: {}", externalLeagueApiId);
        League league = leagueRepository.findByApiLeagueIdAndApiSource(externalLeagueApiId, externalApiSourceName)
                .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + externalLeagueApiId + " and source " + externalApiSourceName + " not found locally. Sync leagues first."));

        // String apiUrl = externalApiBaseUrl + "/leagues/" + externalLeagueApiId + "/teams?apiKey=" + externalApiKey;
        // Hardcoded response
        List<ExternalTeamDto> externalTeams = List.of(
                ExternalTeamDto.builder().apiTeamId("ext-team-A").name("Mock Team Alpha").countryCode("MCK").logoUrl("http://logo.url/alpha.png").foundedYear(1900).stadiumName("Alpha Park").build(),
                ExternalTeamDto.builder().apiTeamId("ext-team-B").name("Mock Team Beta").countryCode("MCK").logoUrl("http://logo.url/beta.png").foundedYear(1920).stadiumName("Beta Field").build()
        );

        if (externalTeams == null || externalTeams.isEmpty()) {
            log.info("No teams found from external API for league API ID: {}", externalLeagueApiId);
            return;
        }

        for (ExternalTeamDto extTeam : externalTeams) {
            Optional<Team> existingTeamOpt = teamRepository.findByApiTeamIdAndApiSource(extTeam.getApiTeamId(), externalApiSourceName);
            Team team;
            if (existingTeamOpt.isPresent()) {
                team = existingTeamOpt.get();
                log.info("Updating existing team: {} (API ID: {})", team.getTeamName(), extTeam.getApiTeamId());
            } else {
                team = new Team();
                team.setApiTeamId(extTeam.getApiTeamId());
                log.info("Creating new team with API ID: {}", extTeam.getApiTeamId());
            }
            team.setTeamName(extTeam.getName());
            team.setCountry(extTeam.getCountryCode()); // Assuming countryCode maps to country string
            team.setLogoUrl(extTeam.getLogoUrl());
            team.setFoundedYear(extTeam.getFoundedYear());
            team.setStadiumName(extTeam.getStadiumName());
            team.setApiSource(externalApiSourceName);
            // Note: Link to League entity is not direct on Team model as per current design.
            // Teams are linked via Fixtures, or if a team has a 'currentLeague' that would be set here.
            teamRepository.save(team);
        }
        log.info("Finished team sync for league {}. Processed {} teams.", league.getLeagueName(), externalTeams.size());
    }

    @Override
    @Transactional
    public void syncFixturesByLeague(String externalLeagueApiId, String season) {
        log.info("Starting fixture sync for externalLeagueApiId: {}, season: {}", externalLeagueApiId, season);
        League league = leagueRepository.findByApiLeagueIdAndApiSource(externalLeagueApiId, externalApiSourceName)
                .orElseThrow(() -> new EntityNotFoundException("League with apiLeagueId " + externalLeagueApiId + " and source " + externalApiSourceName + " not found locally. Sync leagues first."));

        // String apiUrl = externalApiBaseUrl + "/leagues/" + externalLeagueApiId + "/fixtures?season=" + season + "&apiKey=" + externalApiKey;
        // Hardcoded response
        LocalDateTime now = LocalDateTime.now();
        List<ExternalFixtureDto> externalFixtures = List.of(
            ExternalFixtureDto.builder().apiFixtureId("ext-fix-123").apiLeagueId(externalLeagueApiId).apiHomeTeamId("ext-team-A").apiAwayTeamId("ext-team-B").matchTimestamp(now.plusDays(7)).statusShort("NS").round("Round 1").build(),
            ExternalFixtureDto.builder().apiFixtureId("ext-fix-456").apiLeagueId(externalLeagueApiId).apiHomeTeamId("ext-team-B").apiAwayTeamId("ext-team-A").matchTimestamp(now.plusDays(14)).statusShort("NS").goalsHome(1).goalsAway(1).round("Round 2").build()
        );

        if (externalFixtures == null || externalFixtures.isEmpty()) {
            log.info("No fixtures found from external API for league API ID: {} and season: {}", externalLeagueApiId, season);
            return;
        }

        for (ExternalFixtureDto extFixture : externalFixtures) {
            Optional<Team> homeTeamOpt = teamRepository.findByApiTeamIdAndApiSource(extFixture.getApiHomeTeamId(), externalApiSourceName);
            Optional<Team> awayTeamOpt = teamRepository.findByApiTeamIdAndApiSource(extFixture.getApiAwayTeamId(), externalApiSourceName);

            if (homeTeamOpt.isEmpty() || awayTeamOpt.isEmpty()) {
                log.warn("Skipping fixture with API ID: {} because one or both teams not found locally (Home: {}, Away: {}). Sync teams first.",
                        extFixture.getApiFixtureId(), extFixture.getApiHomeTeamId(), extFixture.getApiAwayTeamId());
                continue;
            }

            Optional<Fixture> existingFixtureOpt = fixtureRepository.findByApiFixtureIdAndApiSource(extFixture.getApiFixtureId(), externalApiSourceName);
            Fixture fixture;
            if (existingFixtureOpt.isPresent()) {
                fixture = existingFixtureOpt.get();
                log.info("Updating existing fixture (API ID: {})", extFixture.getApiFixtureId());
            } else {
                fixture = new Fixture();
                fixture.setApiFixtureId(extFixture.getApiFixtureId());
                log.info("Creating new fixture with API ID: {}", extFixture.getApiFixtureId());
            }

            fixture.setLeague(league);
            fixture.setTeam1(homeTeamOpt.get());
            fixture.setTeam2(awayTeamOpt.get());
            fixture.setMatchDatetime(extFixture.getMatchTimestamp());
            fixture.setStatus(extFixture.getStatusShort());
            fixture.setGoalsTeam1(extFixture.getGoalsHome());
            fixture.setGoalsTeam2(extFixture.getGoalsAway());
            fixture.setRound(extFixture.getRound());
            fixture.setApiSource(externalApiSourceName);

            fixtureRepository.save(fixture);
        }
        log.info("Finished fixture sync for league {}. Processed {} fixtures.", league.getLeagueName(), externalFixtures.size());
    }
}
