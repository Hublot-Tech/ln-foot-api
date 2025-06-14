package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultDto {
    private String status; // e.g., "success", "partial_success", "failure"
    private String message; // Detailed message about the sync operation
    private Integer count; // Number of items synced, created, or updated
    private String operationType; // e.g., "syncLeagues", "syncTeamsByLeague"
}
