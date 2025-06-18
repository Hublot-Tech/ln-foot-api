package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNewsArticleDto {
    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title;

    private String content;

    @Size(max = 100, message = "Source name cannot exceed 100 characters.")
    private String source;

    @Size(max = 2048, message = "Article URL is too long.")
    private String url;

    @Size(max = 2048, message = "Image URL is too long.")
    private String imageUrl;

    @PastOrPresent(message = "Publication date must be in the past or present, if provided.")
    private OffsetDateTime publishedAt;

    private List<String> tags;
    private String status;
}
