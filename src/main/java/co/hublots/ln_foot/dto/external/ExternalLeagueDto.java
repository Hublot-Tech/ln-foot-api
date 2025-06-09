package co.hublots.ln_foot.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalLeagueDto {
    private String apiLeagueId; // e.g., "league123" from external API
    private String name;
    private String country; // Country name or code
    private String logoUrl;
    private String sportId; // e.g., "soccer", "basketball"
    private Integer tier; // League tier or level
}
