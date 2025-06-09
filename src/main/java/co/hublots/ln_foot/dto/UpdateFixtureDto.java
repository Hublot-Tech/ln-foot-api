package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFixtureDto {
    private String referee;
    private String timezone;
    private OffsetDateTime date;
    private Integer timestamp;
    private String venueName;
    private String venueCity;
    private String statusShort;
    private String statusLong;
    private Integer elapsed;
    // leagueId, season, round, homeTeamId, awayTeamId are usually not updatable for a fixture
    // but this depends on the specific business logic for zUpdateFixture.
    // For now, assuming they are not updatable.
    private Integer goalsHome;
    private Integer goalsAway;
    private Integer scoreHtHome;
    private Integer scoreHtAway;
    private Integer scoreFtHome;
    private Integer scoreFtAway;
    private Integer scoreEtHome;
    private Integer scoreEtAway;
    private Integer scorePtHome;
    private Integer scorePtAway;
}
