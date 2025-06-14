package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagePresignedUrlResponseDto {
    private String uploadUrl;
    private Map<String, String> formData;
    private String key;
    private String finalUrl;
}
