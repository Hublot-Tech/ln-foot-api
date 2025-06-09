package co.hublots.ln_foot.controllers;

// import co.hublots.ln_foot.dto.SyncFixturesByLeagueDto; // Old DTO
// import co.hublots.ln_foot.dto.SyncLeaguesDto; // Old DTO
// import co.hublots.ln_foot.dto.SyncResultDto; // Old DTO
// import co.hublots.ln_foot.dto.SyncTeamsByLeagueDto; // Old DTO
// import co.hublots.ln_foot.dto.SyncFixtureDetailsDto; // Old DTO
import co.hublots.ln_foot.dto.SyncStatusDto; // New DTO
import co.hublots.ln_foot.services.DataSyncService; // Changed from SyncService
// import co.hublots.ln_foot.services.SyncService; // Old Service
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map; // For request body
import java.util.HashMap; // For default empty map

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final DataSyncService dataSyncService; // Changed from SyncService

    public SyncController(DataSyncService dataSyncService) { // Changed from SyncService
        this.dataSyncService = dataSyncService;
    }

    @PostMapping("/all-fixtures") // New consolidated endpoint
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncStatusDto> syncAllFixtures(@RequestBody(required = false) Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? new HashMap<>() : queryParams;
        SyncStatusDto result = dataSyncService.syncMainFixtures(params);
        if ("ERROR".equals(result.getStatus())) {
            // Consider returning HTTP 500 or specific error code based on result.message
            return ResponseEntity.status(500).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // Remove old endpoints that called deprecated service methods
    /*
    @PostMapping("/leagues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResultDto> syncLeagues(@RequestBody(required = false) SyncLeaguesDto syncLeaguesDto) {
        SyncLeaguesDto payload = syncLeaguesDto != null ? syncLeaguesDto : new SyncLeaguesDto();
        // This would now call the old syncService.syncLeagues which itself calls dataSyncService.syncMainFixtures
        // For a cleaner API, this endpoint should be removed or updated to pass Map<String,String>
        // return ResponseEntity.ok(syncService.syncLeagues(payload)); // Assuming SyncService is DataSyncService
        // For now, let's assume this endpoint is removed in favor of /all-fixtures
        return ResponseEntity.status(HttpStatus.GONE).build();
    }

    @PostMapping("/teams-by-league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResultDto> syncTeamsByLeague(@RequestBody SyncTeamsByLeagueDto syncTeamsByLeagueDto) {
        // return ResponseEntity.ok(syncService.syncTeamsByLeague(syncTeamsByLeagueDto));
        return ResponseEntity.status(HttpStatus.GONE).build();
    }

    @PostMapping("/fixtures-by-league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResultDto> syncFixturesByLeague(@RequestBody SyncFixturesByLeagueDto syncFixturesByLeagueDto) {
        // return ResponseEntity.ok(syncService.syncFixturesByLeague(syncFixturesByLeagueDto));
        return ResponseEntity.status(HttpStatus.GONE).build();
    }

    @PostMapping("/fixture-details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResultDto> syncFixtureDetails(@RequestBody SyncFixtureDetailsDto syncFixtureDetailsDto) {
        // return ResponseEntity.ok(syncService.syncFixtureDetails(syncFixtureDetailsDto));
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
    */
}
