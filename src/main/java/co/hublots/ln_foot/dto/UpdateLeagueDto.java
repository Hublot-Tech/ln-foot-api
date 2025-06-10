package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeagueDto {
    @Size(max = 100, message = "League name cannot exceed 100 characters.")
    private String name; // Optional

    @Size(max = 100, message = "Country name cannot exceed 100 characters.")
    private String country; // Optional

    @URL(message = "Please provide a valid logo URL.")
    @Size(max = 2048, message = "Logo URL is too long.")
    private String logoUrl; // Optional

    @URL(message = "Please provide a valid flag URL.")
    @Size(max = 2048, message = "Flag URL is too long.")
    private String flagUrl; // Optional

    @Size(max = 50, message = "Season description is too long.")
    private String season; // Optional

    @Size(max = 50, message = "League type description is too long.")
    private String type;   // Optional
}
