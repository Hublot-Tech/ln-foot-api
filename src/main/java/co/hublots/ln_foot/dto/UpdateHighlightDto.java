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
public class UpdateHighlightDto {
    // fixtureId is generally not changed for a highlight
    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title; // Optional

    private String description; // Optional

    @URL(message = "Please provide a valid video URL.")
    @Size(max = 2048, message = "Video URL is too long.")
    private String videoUrl; // Optional

    @URL(message = "Please provide a valid thumbnail URL.")
    @Size(max = 2048, message = "Thumbnail URL is too long.")
    private String thumbnailUrl; // Optional

    private Integer durationSeconds; // Optional, maybe @Min(1)
    private String type;   // Optional, maybe @Size
}
