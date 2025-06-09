package co.hublots.ln_foot.scheduler;

import co.hublots.ln_foot.services.DataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.time.Year; // For current year as season placeholder

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSyncScheduler {

    private final DataSyncService dataSyncService;

    // Example: Fetching all leagues from DB to sync (if preferred over hardcoded list)
    // private final LeagueRepository leagueRepository;

    /**
     * Periodically syncs leagues from the external API.
     * Runs daily at 1:00 AM UTC.
     */
    @Scheduled(cron = "0 0 1 * * ?", zone = "UTC")
    public void scheduleLeagueSync() {
        log.info("Starting scheduled league sync...");
        try {
            // Hardcoded parameters for demonstration
            // In a real app, these might come from config or a list of supported sports/countries
            dataSyncService.syncLeagues("soccer", "Mockland");
            // Example: dataSyncService.syncLeagues("basketball", "USA");
            log.info("Scheduled league sync completed successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled league sync:", e);
        }
    }

    /**
     * Periodically syncs teams for known leagues from the external API.
     * Runs daily at 2:00 AM UTC, after presumed league sync.
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "UTC")
    public void scheduleTeamSync() {
        log.info("Starting scheduled team sync...");
        try {
            // Using the same externalLeagueApiIds that DataSyncServiceImpl's mock response uses
            List<String> leagueApiIdsToSync = List.of("ext-league-1", "ext-league-2");

            // Alternative: Fetch all leagues from your DB and sync teams for them
            // List<League> leagues = leagueRepository.findAll();
            // for (League league : leagues) {
            //    if (league.getApiLeagueId() != null && league.getApiSource().equals(externalApiSourceName)) { // Check source
            //        log.info("Syncing teams for league: {} (API ID: {})", league.getLeagueName(), league.getApiLeagueId());
            //        dataSyncService.syncTeamsByLeague(league.getApiLeagueId());
            //    }
            // }

            for (String leagueApiId : leagueApiIdsToSync) {
                log.info("Syncing teams for league API ID: {}", leagueApiId);
                dataSyncService.syncTeamsByLeague(leagueApiId);
            }
            log.info("Scheduled team sync completed successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled team sync:", e);
        }
    }

    /**
     * Periodically syncs fixtures for known leagues from the external API.
     * Runs every hour at the start of the hour.
     */
    @Scheduled(cron = "0 0 * * * ?", zone = "UTC")
    public void scheduleFixtureSync() {
        log.info("Starting scheduled fixture sync...");
        try {
            List<String> leagueApiIdsToSync = List.of("ext-league-1", "ext-league-2");
            String currentSeason = String.valueOf(Year.now().getValue()); // e.g., "2024"

            for (String leagueApiId : leagueApiIdsToSync) {
                log.info("Syncing fixtures for league API ID: {} for season: {}", leagueApiId, currentSeason);
                dataSyncService.syncFixturesByLeague(leagueApiId, currentSeason);
            }
            log.info("Scheduled fixture sync completed successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled fixture sync:", e);
        }
    }
}
