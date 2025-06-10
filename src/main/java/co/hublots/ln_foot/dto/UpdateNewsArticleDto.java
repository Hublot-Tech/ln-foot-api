package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.URL;
// NotBlank/NotEmpty/NotNull not usually on optional update fields unless logic requires it for specific cases

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNewsArticleDto {
    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title; // Optional

    private String content; // Optional, if blank means no change or clear, depending on service logic

    private String authorId; // Optional

    @Size(max = 100, message = "Source name cannot exceed 100 characters.")
    private String source; // Optional

    @URL(message = "Please provide a valid URL for the article.")
    @Size(max = 2048, message = "Article URL is too long.")
    private String url; // Optional

    @URL(message = "Please provide a valid URL for the image.")
    @Size(max = 2048, message = "Image URL is too long.")
    private String imageUrl; // Optional

    @PastOrPresent(message = "Publication date must be in the past or present, if provided.")
    private OffsetDateTime publishedAt; // Optional

    private List<String> tags; // Optional
    private String status;     // Optional
}
