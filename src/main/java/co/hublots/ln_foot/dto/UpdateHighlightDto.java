package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHighlightDto {
    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title;

    private String description;

    @Size(max = 2048, message = "Video URL is too long.")
    private String videoUrl;

    @Size(max = 2048, message = "Thumbnail URL is too long.")
    private String thumbnailUrl;

    private Integer durationSeconds;
    private String type;
}
