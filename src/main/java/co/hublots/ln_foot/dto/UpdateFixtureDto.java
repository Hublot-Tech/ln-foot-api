package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
// import jakarta.validation.constraints.NotBlank; // Not typically used for optional update fields

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFixtureDto {
    private String referee; // Optional
    private String timezone; // Optional

    @FutureOrPresent(message = "Match date/time must be in the present or future, if provided.")
    private OffsetDateTime date; // Optional

    private Integer timestamp; // Optional
    private String venueName; // Optional
    private String venueCity; // Optional
    private String statusShort; // Optional
    private String statusLong;  // Optional
    private Integer elapsed;    // Optional

    // leagueId, season, round, homeTeamId, awayTeamId are usually not updatable for a fixture's main details.
    // These would be part of specific operations if allowed (e.g. reschedule involving season/league change is rare).

    @Min(value = 0, message = "Home goals cannot be negative.")
    private Integer goalsHome;

    @Min(value = 0, message = "Away goals cannot be negative.")
    private Integer goalsAway;

    @Min(value = 0, message = "Score cannot be negative.")
    private Integer scoreHtHome;
    @Min(value = 0, message = "Score cannot be negative.")
    private Integer scoreHtAway;
    @Min(value = 0, message = "Score cannot be negative.")
    private Integer scoreFtHome;
    @Min(value = 0, message = "Score cannot be negative.")
    private Integer scoreFtAway;
    @Min(value = 0, message = "Score cannot be negative.")
    private Integer scoreEtHome;
    @Min(value = 0, message = "Score cannot be negative.")
    private Integer scoreEtAway;
    @Min(value = 0, message = "Score cannot be negative.")
    private Integer scorePtHome;
    @Min(value = 0, message = "Score cannot be negative.")
    private Integer scorePtAway;
}
