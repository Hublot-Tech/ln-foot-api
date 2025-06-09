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
public class UpdateNewsArticleDto {
    private String title;
    private String content;
    private String author;
    private String source;
    private String url;
    private String imageUrl;
    private OffsetDateTime publishedAt;
    private List<String> tags;
    private String status;
}
