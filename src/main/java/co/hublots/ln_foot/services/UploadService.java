package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;

public interface UploadService {
    ImagePresignedUrlResponseDto getImagePresignedUrl(ImagePresignedUrlRequestDto requestDto);
    void deleteImage(DeleteImageDto deleteDto);
}
