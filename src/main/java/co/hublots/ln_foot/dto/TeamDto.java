package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime; // Added for potential full TeamDto, will be removed if not in zTeamSchema
import java.util.List; // Added for potential full TeamDto, will be removed if not in zTeamSchema

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {
    private String id; // Assuming this is the apiFootballId for teams as well
    private String name;
    private String country;
    private Integer founded;
    private Boolean national;
    private String logoUrl;
    private String venueName;
    private String venueAddress;
    private String venueCity;
    private Integer venueCapacity;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    // private List<FixtureDto> fixtures; // This would cause circular dependency if FixtureDto also has TeamDto. Usually handled by separate queries or simplified DTOs.
}
