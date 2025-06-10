package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.URL; // For imageUrl and url
// import jakarta.validation.constraints.NotNull; // If publishedAt is strictly required

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewsArticleDto {
    @NotBlank(message = "Title cannot be blank.")
    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title;

    @NotBlank(message = "Content cannot be blank.")
    private String content;

    private String authorId; // Optional, can be system/anonymous or linked later

    @Size(max = 100, message = "Source name cannot exceed 100 characters.")
    private String source; // Name of the source publication e.g. "BBC Sport"

    @URL(message = "Please provide a valid URL for the article.")
    @Size(max = 2048, message = "Article URL is too long.")
    private String url; // URL to the original article

    @URL(message = "Please provide a valid URL for the image.")
    @Size(max = 2048, message = "Image URL is too long.")
    private String imageUrl; // Optional

    @PastOrPresent(message = "Publication date must be in the past or present, if provided.")
    private OffsetDateTime publishedAt; // Optional, might be set on publish

    private List<String> tags; // Optional
    private String status;     // Optional, might default in service
}
