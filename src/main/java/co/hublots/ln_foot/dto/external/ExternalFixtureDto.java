package co.hublots.ln_foot.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Using LocalDateTime for simplicity from external API

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalFixtureDto {
    private String apiFixtureId; // e.g., "fixXYZ"
    private String apiLeagueId;  // External API's ID for the league
    private String apiHomeTeamId; // External API's ID for home team
    private String apiAwayTeamId; // External API's ID for away team
    private LocalDateTime matchTimestamp; // Assuming API provides UTC or server-consistent time
    private String statusShort; // e.g., "NS", "FT", "1H"
    private Integer goalsHome;
    private Integer goalsAway;
    private String round; // e.g., "Regular Season - 10"
    // Other fields like venue, referee could be here
}
