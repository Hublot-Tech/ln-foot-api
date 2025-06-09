package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeagueDto {
    private String name;
    private String country;
    private String logoUrl;
    private String flagUrl;
    private String season; // Season might be updatable, e.g. promoting to new season records
    private String type;
}
