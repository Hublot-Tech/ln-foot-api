package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {
    private String id;
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
}
