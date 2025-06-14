package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureResponseItemDto {
    private ExternalFixtureDetailsDto fixture;
    private ExternalLeagueInFixtureDto league;
    private TeamsInFixtureDto teams;
    private GoalsDto goals;
    private ScoreDto score;
}
