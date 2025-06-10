package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.SyncStatusDto;
import java.util.Map;
import reactor.core.publisher.Mono; // Moved import to the top

public interface DataSyncService {
    // Keep old methods for now as they are called by scheduler, they will call syncMainFixtures
    void syncLeagues(String sportId, String countryName);
    void syncTeamsByLeague(String externalLeagueApiId);
    void syncFixturesByLeague(String externalLeagueApiId, String season);

    Mono<SyncStatusDto> syncMainFixtures(Map<String, String> queryParams);
    // void syncFixtureDetails(String externalFixtureApiId); // Deferred
}
