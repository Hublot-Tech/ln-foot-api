package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncLeaguesDto {
    // Optional: if we want to sync specific leagues by their external IDs
    private List<String> externalLeagueIds;
    // Optional: a flag to indicate a full resync or an incremental one
    private Boolean fullResync;
}
