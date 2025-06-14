package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncTeamsByLeagueDto {
    private String leagueId; // Internal or external league ID
    private String season; // Season for which to sync teams, as teams can change season over season
}
