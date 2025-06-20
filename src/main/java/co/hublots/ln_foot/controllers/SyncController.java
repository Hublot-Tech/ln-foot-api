package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.SyncStatusDto;
import co.hublots.ln_foot.dto.SyncStatusDto.SyncStatus;
import co.hublots.ln_foot.services.DataSyncService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final DataSyncService dataSyncService;

    @PostMapping("/all-fixtures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncStatusDto> syncAllFixtures(
            @RequestBody(required = false) Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? new HashMap<>() : queryParams;
        SyncStatusDto result = dataSyncService.syncMainFixtures(params);
        if (SyncStatus.ERROR.equals(result.getStatus())) {
            return ResponseEntity.status(500).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
