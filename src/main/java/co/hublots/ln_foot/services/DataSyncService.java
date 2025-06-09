package co.hublots.ln_foot.services;

public interface DataSyncService {
    void syncLeagues(String sportId, String countryName);
    void syncTeamsByLeague(String externalLeagueApiId);
    void syncFixturesByLeague(String externalLeagueApiId, String season);
    // void syncFixtureDetails(String externalFixtureApiId); // Deferred
}
