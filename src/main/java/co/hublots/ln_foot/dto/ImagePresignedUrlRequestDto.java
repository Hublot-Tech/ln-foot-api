package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagePresignedUrlRequestDto {
    private String fileName; // e.g., "team-logo.png"
    private String contentType; // e.g., "image/png"
    private Long contentLength; // Optional: size of the file in bytes, might be needed by S3
    private String entityType; // e.g., "team", "league", "advertisement", "newsArticle"
    private String entityId; // Optional: ID of the entity this image is for
}
