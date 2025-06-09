package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    private String id;
    private String title;
    private String content; // Can be HTML or markdown
    private UserDto author; // Changed from String author
    private String sourceName; // e.g., "BBC Sport", "Sky Sports" (was 'source')
    private String articleUrl; // URL to the original article (was 'url')
    private String imageUrl;
    private OffsetDateTime publishedAt;
    private List<String> tags; // e.g., ["transfer", "injury", "match-report"]
    private String status; // e.g., "published", "draft"
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
