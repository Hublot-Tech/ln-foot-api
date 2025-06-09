package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusDto {
    @JsonProperty("long") // "long" is a keyword in Java
    private String longStatus;

    @JsonProperty("short") // "short" is a keyword in Java
    private String shortStatus;

    private Integer elapsed; // Elapsed minutes in the game, null if not started/applicable
}
