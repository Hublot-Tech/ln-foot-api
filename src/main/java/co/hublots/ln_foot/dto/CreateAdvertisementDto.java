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
public class CreateAdvertisementDto {
    private String title;
    private String content;
    private String referenceUrl;
    private String imageUrl;
    private String videoUrl;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Integer priority;
    private String status;
}
