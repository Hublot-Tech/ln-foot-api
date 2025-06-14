package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteImageDto {
    /**
     * The S3 object key of the image to delete.
     * Provide this OR imageUrl, but not both.
     */
    private String key;

    /**
     * The full URL of the image to delete. The backend service will attempt to parse the object key from this URL.
     * Provide this OR key, but not both.
     */
    private String imageUrl;

    // TODO: Implement class-level validation to ensure either 'key' or 'imageUrl' is provided, but not both.
    // For now, service layer (UploadServiceImpl) should handle this logic.

    private String entityType; // Optional: for logging or specific deletion logic
    private String entityId; // Optional: for logging or specific deletion logic
}
