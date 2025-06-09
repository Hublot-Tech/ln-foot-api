package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime; // Or ZonedDateTime if API guarantees zone info

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalFixtureDetailsDto {
    @JsonProperty("id")
    private long fixtureApiId; // Renamed from 'id' to avoid confusion if used as an entity field

    private String referee;
    private String timezone; // e.g., "UTC"
    private OffsetDateTime date; // Full date-time string from API
    private long timestamp; // Unix timestamp (seconds)

    private PeriodsDto periods;
    private VenueDto venue;
    private StatusDto status;
}
