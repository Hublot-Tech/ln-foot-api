package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
import java.util.Map;

@AllArgsConstructor
public class ImagePresignedUrlResponseDto {
    private String uploadUrl; // The URL to POST the form to (Minio server bucket URL)
    private Map<String, String> formData; // The form fields to include in the POST
    private String key; // The S3 key for the uploaded file (also included in formData)
    private String finalUrl; // Optional: The public URL of the image after upload
}
