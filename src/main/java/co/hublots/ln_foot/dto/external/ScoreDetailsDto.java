package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoreDetailsDto {
    private Integer home; // Goals for home team in this period
    private Integer away; // Goals for away team in this period
}
