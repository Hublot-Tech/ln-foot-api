package co.hublots.ln_foot.dto;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String authorName;

    @Size(max = 100, message = "Source name cannot exceed 100 characters.")
    private String source; // Name of the source publication e.g. "BBC Sport"

    @Size(max = 2048, message = "Article URL is too long.")
    private String url;

    @Size(max = 2048, message = "Image URL is too long.")
    private String imageUrl;

    @PastOrPresent(message = "Publication date must be in the past or present, if provided.")
    private OffsetDateTime publishedAt;

    private List<String> tags;
    private String status;
}
