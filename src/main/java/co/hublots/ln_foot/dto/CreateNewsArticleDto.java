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
public class CreateNewsArticleDto {
    private String title;
    private String content;
    private String authorId; // Changed from author
    private String source; // Name of the source publication e.g. "BBC Sport"
    private String url; // URL to the original article
    private String imageUrl;
    private OffsetDateTime publishedAt;
    private List<String> tags;
    private String status;
}
