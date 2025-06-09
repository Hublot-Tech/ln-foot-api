package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalLeagueInFixtureDto {
    @JsonProperty("id")
    private long leagueApiId;

    private String name;
    private String country;
    private String logo;
    private String flag;
    private int season; // Typically the year, e.g., 2023
}
