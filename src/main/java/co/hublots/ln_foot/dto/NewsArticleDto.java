package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import co.hublots.ln_foot.models.NewsArticle.NewsStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    private String id;
    private String title;
    private String content; // Can be HTML or markdown
    private String summary; 
    private String authorName; // e.g., "John Doe" (was 'authorId', now just name for simplicity)
    private Boolean isMajorUpdate;
    private String sourceUrl; // URL to the original article (was 'url')
    private String imageUrl;
    private OffsetDateTime publishedAt;
    private List<String> tags; // e.g., ["transfer", "injury", "match-report"]
    private NewsStatus status; // e.g., "published", "draft"
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
