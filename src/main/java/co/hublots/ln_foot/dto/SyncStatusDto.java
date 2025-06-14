package co.hublots.ln_foot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusDto {
    private String status; // e.g., "SUCCESS", "ERROR", "NO_DATA"
    private String message;
    private Integer itemsProcessed; // Number of primary items processed (e.g., fixtures)
}
