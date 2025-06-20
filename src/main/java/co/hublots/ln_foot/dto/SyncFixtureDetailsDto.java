package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncFixtureDetailsDto {
    private String fixtureId; // Internal or external fixture ID
    // Could also include a flag if we need to force update even if recently synced
    private Boolean forceUpdate;
}
