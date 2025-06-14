package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeagueDto {
    @NotBlank(message = "League API ID (id) must be provided.")
    private String id; // apiFootballId

    @NotBlank(message = "League name must be provided.")
    @Size(max = 100, message = "League name cannot exceed 100 characters.")
    private String name;

    @NotBlank(message = "Country must be provided.")
    @Size(max = 100, message = "Country name cannot exceed 100 characters.")
    private String country;

    @Size(max = 2048, message = "Logo URL is too long.")
    private String logoUrl; // Optional, but if provided, must be URL

    @Size(max = 2048, message = "Flag URL is too long.")
    private String flagUrl; // Optional

    @Size(max = 50, message = "Season description is too long.")
    private String season; // Optional

    @Size(max = 50, message = "League type description is too long.")
    private String type;   // Optional
}
