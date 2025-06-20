package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeagueDto {
    @Size(max = 100, message = "League name cannot exceed 100 characters.")
    private String name;

    @Size(max = 100, message = "Country name cannot exceed 100 characters.")
    private String country;

    @Size(max = 2048, message = "Logo URL is too long.")
    private String logoUrl;

    @Size(max = 2048, message = "Flag URL is too long.")
    private String flagUrl;

    @Size(max = 50, message = "Season description is too long.")
    private String season;

    @Size(max = 50, message = "League type description is too long.")
    private String type;  
}
