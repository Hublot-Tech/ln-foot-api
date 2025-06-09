package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoreDto {
    private ScoreDetailsDto halftime;
    private ScoreDetailsDto fulltime;
    private ScoreDetailsDto extratime; // Null if no extra time
    private ScoreDetailsDto penalty;   // Null if no penalty shootout
}
