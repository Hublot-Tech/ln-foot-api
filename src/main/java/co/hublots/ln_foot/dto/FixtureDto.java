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
public class FixtureDto {
    private String id; // Corresponds to apiFootballId
    private String referee;
    private String timezone;
    private OffsetDateTime date;
    private Integer timestamp;
    private String venueName;
    private String venueCity;
    // private String statusShort; // Renamed
    // private String statusLong; // To be replaced by statusDescription
    private String statusShortCode; // Renamed from statusShort
    private String statusDescription; // New field
    private boolean isLive; // New field, derived from status

    private Integer elapsed;
    private String leagueId; // Corresponds to apiFootballId of the league
    private String season; // e.g., "2023"
    private String round;
    private SimpleTeamDto homeTeam;
    private SimpleTeamDto awayTeam;
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
    // private Boolean live; // Replaced by isLive derived from FixtureStatus enum
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
