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
public class CreateFixtureDto {
    private String id; // apiFootballId, required for creation if we're syncing from an external source
    private String referee;
    private String timezone;
    private OffsetDateTime date;
    private Integer timestamp;
    private String venueName;
    private String venueCity;
    private String statusShort;
    private String statusLong;
    private Integer elapsed;
    private String leagueId; // apiFootballId of the league
    private String season;
    private String round;
    private String homeTeamId; // Id for home team
    private String awayTeamId; // Id for away team
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
