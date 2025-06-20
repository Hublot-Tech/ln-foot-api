package co.hublots.ln_foot.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueDto {
    private String id;
    private String name;
    private String country;
    private String logoUrl;
    private String flagUrl;
    private String season;
    private String type; 
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
