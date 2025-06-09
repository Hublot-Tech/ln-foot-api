package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueDto {
    private String id; // Corresponds to apiFootballId
    private String name;
    private String country;
    private String logoUrl;
    private String flagUrl;
    private String season; // e.g., "2023"
    private String type; // e.g., "League", "Cup"
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<FixtureDto> fixtures; // List of fixtures for this league
}
