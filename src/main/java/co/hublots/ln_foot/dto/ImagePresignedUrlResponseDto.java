package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagePresignedUrlResponseDto {
    private String uploadUrl; // The presigned URL for PUT request
    private String key; // The S3 key for the uploaded file
    private String finalUrl; // The public URL of the image after upload (if applicable)
}
