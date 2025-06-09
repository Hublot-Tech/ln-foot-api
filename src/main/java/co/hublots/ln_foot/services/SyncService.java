package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.SyncFixturesByLeagueDto;
import co.hublots.ln_foot.dto.SyncLeaguesDto;
import co.hublots.ln_foot.dto.SyncResultDto;
import co.hublots.ln_foot.dto.SyncTeamsByLeagueDto;
import co.hublots.ln_foot.dto.SyncFixtureDetailsDto; // Corrected this import

public interface SyncService {
    SyncResultDto syncLeagues(SyncLeaguesDto syncLeaguesDto);
    SyncResultDto syncTeamsByLeague(SyncTeamsByLeagueDto syncTeamsByLeagueDto);
    SyncResultDto syncFixturesByLeague(SyncFixturesByLeagueDto syncFixturesByLeagueDto);
    SyncResultDto syncFixtureDetails(SyncFixtureDetailsDto syncFixtureDetailsDto);
}
