package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoalsDto {
    private Integer home; // Goals scored by home team (final score)
    private Integer away; // Goals scored by away team (final score)
}
