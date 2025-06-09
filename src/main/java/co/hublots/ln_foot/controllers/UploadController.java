package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;
import co.hublots.ln_foot.services.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/image-presigned-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImagePresignedUrlResponseDto> getImagePresignedUrl(@RequestBody ImagePresignedUrlRequestDto requestDto) {
        ImagePresignedUrlResponseDto responseDto = uploadService.getImagePresignedUrl(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@RequestBody DeleteImageDto deleteDto) {
        uploadService.deleteImage(deleteDto);
        return ResponseEntity.noContent().build();
    }
}
