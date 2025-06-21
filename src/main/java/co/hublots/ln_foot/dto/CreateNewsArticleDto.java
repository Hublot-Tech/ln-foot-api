package co.hublots.ln_foot.dto;

import java.time.OffsetDateTime;
import java.util.List;

import co.hublots.ln_foot.models.NewsArticle.NewsStatus;
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

    @NotBlank(message = "Summary cannot be blank.")
    @Size(max = 1000, message = "Summary cannot exceed 1000 characters.")
    private String summary;

    private String authorName;

    @Size(max = 2048, message = "Source URL is too long.")
    private String sourceUrl;

    @Size(max = 2048, message = "Image URL is too long.")
    private String imageUrl;

    @PastOrPresent(message = "Publication date must be in the past or present, if provided.")
    private OffsetDateTime publishedAt;

    private List<String> tags;
    private NewsStatus status;

    @Builder.Default
    private Boolean isMajorUpdate = false; // Indicates if this is a significant news update
}
