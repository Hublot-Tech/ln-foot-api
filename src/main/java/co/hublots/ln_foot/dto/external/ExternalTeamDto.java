package co.hublots.ln_foot.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTeamDto {
    private String apiTeamId; // e.g., "teamA789" from external API
    private String name;
    private String countryCode; // e.g., "GB", "ES"
    private String logoUrl;
    private Integer foundedYear;
    private String stadiumName;
    // private String apiLeagueId; // If the API provides current league ID directly with team details
}
