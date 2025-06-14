package co.hublots.ln_foot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHighlightDto {
    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title; // Optional, but if provided, has size limit

    @NotBlank(message = "Video URL must be provided.")
    @Size(max = 2048, message = "Video URL is too long.")
    private String videoUrl;

    @Size(max = 2048, message = "Thumbnail URL is too long.")
    private String thumbnailUrl; // Optional

    private String description; // Optional
    private Integer durationSeconds; // Optional, maybe @Min(1)
    private String type; // Optional, maybe @Size
}
