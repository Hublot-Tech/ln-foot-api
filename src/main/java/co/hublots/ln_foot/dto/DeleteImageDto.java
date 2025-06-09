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
    private String key; // The S3 key of the image to delete
    // OR
    private String imageUrl; // The full URL of the image to delete (backend can parse key from it)
    private String entityType; // Optional: for logging or specific deletion logic
    private String entityId; // Optional: for logging or specific deletion logic
}
