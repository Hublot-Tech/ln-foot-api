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
public class HighlightDto {
    private String id;
    private String title;
    private String description;
    private String videoUrl; // URL to the highlight video
    private String thumbnailUrl; // Optional thumbnail for the video
    private Integer durationSeconds; // Duration of the highlight
    private String type; // e.g., "goal", "foul", "save", "summary"
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
