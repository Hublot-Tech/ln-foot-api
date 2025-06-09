package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeagueDto {
    private String id; // apiFootballId, potentially required if syncing
    private String name;
    private String country;
    private String logoUrl;
    private String flagUrl;
    private String season;
    private String type;
}
