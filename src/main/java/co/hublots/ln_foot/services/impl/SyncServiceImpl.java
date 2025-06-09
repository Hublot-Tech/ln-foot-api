package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.SyncFixturesByLeagueDto;
import co.hublots.ln_foot.dto.SyncLeaguesDto;
import co.hublots.ln_foot.dto.SyncResultDto;
import co.hublots.ln_foot.dto.SyncTeamsByLeagueDto;
import co.hublots.ln_foot.dto.SyncFixtureDetailsDto;
import co.hublots.ln_foot.services.SyncService;
import org.springframework.stereotype.Service;

@Service
public class SyncServiceImpl implements SyncService {

    @Override
    public SyncResultDto syncLeagues(SyncLeaguesDto syncLeaguesDto) {
        // Mock implementation
        int count = syncLeaguesDto.getExternalLeagueIds() != null ? syncLeaguesDto.getExternalLeagueIds().size() : 5;
        if (Boolean.TRUE.equals(syncLeaguesDto.getFullResync())) {
             return SyncResultDto.builder()
                .status("success")
                .message("Successfully performed full resync of " + count + " leagues.")
                .count(count)
                .operationType("syncLeaguesFullResync")
                .build();
        }
        return SyncResultDto.builder()
                .status("success")
                .message("Successfully synced " + count + " leagues.")
                .count(count)
                .operationType("syncLeagues")
                .build();
    }

    @Override
    public SyncResultDto syncTeamsByLeague(SyncTeamsByLeagueDto syncTeamsByLeagueDto) {
        // Mock implementation
        return SyncResultDto.builder()
                .status("success")
                .message("Successfully synced teams for league " + syncTeamsByLeagueDto.getLeagueId() + " for season " + syncTeamsByLeagueDto.getSeason())
                .count(15) // Mock count
                .operationType("syncTeamsByLeague")
                .build();
    }

    @Override
    public SyncResultDto syncFixturesByLeague(SyncFixturesByLeagueDto syncFixturesByLeagueDto) {
        // Mock implementation
        String message = "Successfully synced fixtures for league " + syncFixturesByLeagueDto.getLeagueId();
        if (syncFixturesByLeagueDto.getSeason() != null) {
            message += " for season " + syncFixturesByLeagueDto.getSeason();
        }
        if (syncFixturesByLeagueDto.getDateFrom() != null && syncFixturesByLeagueDto.getDateTo() != null) {
            message += " from " + syncFixturesByLeagueDto.getDateFrom() + " to " + syncFixturesByLeagueDto.getDateTo();
        }
        return SyncResultDto.builder()
                .status("success")
                .message(message)
                .count(25) // Mock count
                .operationType("syncFixturesByLeague")
                .build();
    }

    @Override
    public SyncResultDto syncFixtureDetails(SyncFixtureDetailsDto syncFixtureDetailsDto) {
        String message = "Successfully synced details for fixture " + syncFixtureDetailsDto.getFixtureId() + ".";
        if (Boolean.TRUE.equals(syncFixtureDetailsDto.getForceUpdate())) {
            message += " (Forced update)";
        }
        return SyncResultDto.builder()
                .status("success")
                .message(message)
                .count(1)
                .operationType("syncFixtureDetails")
                .build();
    }
}
