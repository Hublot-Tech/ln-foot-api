package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.SyncFixturesByLeagueDto;
import co.hublots.ln_foot.dto.SyncLeaguesDto;
import co.hublots.ln_foot.dto.SyncResultDto;
import co.hublots.ln_foot.dto.SyncTeamsByLeagueDto;
import co.hublots.ln_foot.dto.SyncFixtureDetailsDto;
import co.hublots.ln_foot.services.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/leagues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResultDto> syncLeagues(@RequestBody(required = false) SyncLeaguesDto syncLeaguesDto) {
        // If syncLeaguesDto is null (empty body), create a default one or handle in service
        SyncLeaguesDto payload = syncLeaguesDto != null ? syncLeaguesDto : new SyncLeaguesDto();
        return ResponseEntity.ok(syncService.syncLeagues(payload));
    }

    @PostMapping("/teams-by-league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResultDto> syncTeamsByLeague(@RequestBody SyncTeamsByLeagueDto syncTeamsByLeagueDto) {
        return ResponseEntity.ok(syncService.syncTeamsByLeague(syncTeamsByLeagueDto));
    }

    @PostMapping("/fixtures-by-league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResultDto> syncFixturesByLeague(@RequestBody SyncFixturesByLeagueDto syncFixturesByLeagueDto) {
        return ResponseEntity.ok(syncService.syncFixturesByLeague(syncFixturesByLeagueDto));
    }

    @PostMapping("/fixture-details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResultDto> syncFixtureDetails(@RequestBody SyncFixtureDetailsDto syncFixtureDetailsDto) {
        return ResponseEntity.ok(syncService.syncFixtureDetails(syncFixtureDetailsDto));
    }
}
