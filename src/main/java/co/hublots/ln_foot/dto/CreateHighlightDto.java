package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // For fixtureId if it's not a string, assuming it's an API ID string
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL; // Using Hibernate's @URL

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHighlightDto {
    @NotBlank(message = "Fixture ID must be provided.")
    private String fixtureId; // Assuming this is the apiFixtureId (string)

    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title; // Optional, but if provided, has size limit

    @NotBlank(message = "Video URL must be provided.")
    @URL(message = "Please provide a valid video URL.")
    @Size(max = 2048, message = "Video URL is too long.")
    private String videoUrl;

    @URL(message = "Please provide a valid thumbnail URL.")
    @Size(max = 2048, message = "Thumbnail URL is too long.")
    private String thumbnailUrl; // Optional

    private String description; // Optional
    private Integer durationSeconds; // Optional, maybe @Min(1)
    private String type; // Optional, maybe @Size
}
