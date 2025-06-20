package co.hublots.ln_foot.services;

import java.util.Map;

import co.hublots.ln_foot.dto.SyncStatusDto;

public interface DataSyncService {
    void syncLeagues(String sportId, String countryName);
    void syncTeamsByLeague(String externalLeagueApiId);
    void syncFixturesByLeague(String externalLeagueApiId, String season);

    SyncStatusDto syncMainFixtures(Map<String, String> queryParams);
}
