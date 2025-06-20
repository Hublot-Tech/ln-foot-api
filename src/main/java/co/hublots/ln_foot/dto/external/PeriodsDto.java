package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeriodsDto {
    private Long first; // Timestamp for start of first half
    private Long second; // Timestamp for start of second half
    // Some APIs might provide end timestamps too, e.g., firstHalfEnd, secondHalfEnd
}
