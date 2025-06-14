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
public class AdvertisementDto {
    private String id;
    private String title;
    private String content;
    private String url;
    private String imageUrl;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Integer priority;
    private String status; // e.g., "active", "inactive", "scheduled"
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
