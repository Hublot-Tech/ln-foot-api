package co.hublots.ln_foot.dto;

import java.time.OffsetDateTime;
import java.util.List;

import co.hublots.ln_foot.models.NewsArticle.NewsStatus;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNewsArticleDto {
    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title;

    @Size(max = 5000, message = "Content cannot exceed 5000 characters.")
    private String content;

    @Pattern(regexp = "^(https?://).*", message = "URL must start with http:// or https://")
    @Size(max = 2048, message = "Article URL is too long.")
    private String sourceUrl;

    @Pattern(regexp = "^(https?://).*", message = "Image URL must start with http:// or https://")
    @Size(max = 2048, message = "Image URL is too long.")
    private String imageUrl;

    @PastOrPresent(message = "Publication date must be in the past or present, if provided.")
    private OffsetDateTime publishedAt;

    @Size(max = 10, message = "Cannot have more than 10 tags")
    private List<String> tags;

    @Pattern(regexp = "^(DRAFT|PUBLISHED|ARCHIVED)$", message = "Status must be DRAFT, PUBLISHED, or ARCHIVED")
    private NewsStatus status;
}
