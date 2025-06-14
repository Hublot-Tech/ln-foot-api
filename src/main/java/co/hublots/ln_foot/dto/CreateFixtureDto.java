package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Size; // If needed for string lengths

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFixtureDto {
    @NotBlank(message = "Fixture API ID (id) must be provided.")
    private String id; // apiFootballId

    private String referee; // Optional
    private String timezone; // Optional

    @NotNull(message = "Match date/time must be provided.")
    @FutureOrPresent(message = "Match date/time must be in the present or future.")
    private OffsetDateTime date;

    private Integer timestamp; // Often derived from date, or vice-versa. Optional if 'date' is primary.
    private String venueName; // Optional
    private String venueCity; // Optional

    @NotBlank(message = "Short status must be provided.")
    private String statusShort;
    private String statusLong; // Optional, can be derived from shortStatus
    private Integer elapsed;   // Optional, usually for live games

    @NotBlank(message = "League API ID (leagueId) must be provided.")
    private String leagueId;

    private String season; // Optional, context might come from league or date
    private String round;  // Optional

    @NotBlank(message = "Home team API ID (homeTeamId) must be provided.")
    private String homeTeamId;

    @NotBlank(message = "Away team API ID (awayTeamId) must be provided.")
    private String awayTeamId;

    @Min(value = 0, message = "Home goals cannot be negative.")
    private Integer goalsHome; // Optional at creation, defaults to null/0

    @Min(value = 0, message = "Away goals cannot be negative.")
    private Integer goalsAway; // Optional at creation, defaults to null/0

    // Other score fields (scoreHtHome, etc.) are usually not set at initial creation
    // and are optional, with @Min(0) if present.
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
