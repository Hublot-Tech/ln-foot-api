package co.hublots.ln_foot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.hublots.ln_foot.services.DataSyncService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSyncScheduler {

    private final DataSyncService dataSyncService;

    /**
     * Periodically performs a comprehensive sync of fixtures, typically for the current day.
     * Configurable via `application.sync.dailyCron`, defaults to daily at 00:05 AM UTC.
     */
    @Scheduled(cron = "${application.sync.dailyCron:0 5 0 * * ?}", zone = "UTC")
    public void scheduleDailyFullSync() {
        log.info("Starting scheduled daily full fixture sync...");
        try {
            Map<String, String> queryParams = new HashMap<>();
            // Sync fixtures for the current day in UTC.
            // The specific parameter name ("date") depends on the external API.
            queryParams.put("date", LocalDate.now(ZoneOffset.UTC).toString());

            // Potentially add more parameters if needed for a "full" sync,
            // e.g., specific leagues if `interestedLeagues` filtering in service is not sufficient
            // or if API requires league ID for date-based queries.
            // For now, relying on date and the service-side interestedLeagues filtering.

            dataSyncService.syncMainFixtures(queryParams);
            log.info("Scheduled daily full fixture sync completed successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled daily full fixture sync:", e);
        }
    }

    /**
     * Periodically syncs recent or live fixtures.
     * Configurable via `application.sync.hourlyCron`, defaults to hourly at the start of the hour UTC.
     */
    @Scheduled(cron = "${application.sync.hourlyCron:0 0 */1 * * ?}", zone = "UTC")
    public void scheduleHourlyRecentFixturesSync() {
        log.info("Starting scheduled hourly recent fixtures sync...");
        try {
            Map<String, String> queryParams = new HashMap<>();
            // Parameter "live=all" is hypothetical and depends on the external API supporting it.
            // If not supported, this might re-fetch fixtures for the current date,
            // and the `DataSyncServiceImpl` would handle updates based on changed data.
            // Alternatively, this could fetch fixtures for a very narrow recent time window.
            queryParams.put("live", "all");
            // Or, for example, fetch today's fixtures again, hoping for live updates:
            // queryParams.put("date", LocalDate.now(ZoneOffset.UTC).toString());

            dataSyncService.syncMainFixtures(queryParams);
            log.info("Scheduled hourly recent fixtures sync completed successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled hourly recent fixtures sync:", e);
        }
    }
}
