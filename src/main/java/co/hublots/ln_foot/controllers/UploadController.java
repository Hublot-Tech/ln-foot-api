package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;
import co.hublots.ln_foot.services.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/image-presigned-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImagePresignedUrlResponseDto> getImagePresignedUrl(
            @Valid @RequestBody ImagePresignedUrlRequestDto requestDto) {
        ImagePresignedUrlResponseDto responseDto = uploadService.getImagePresignedUrl(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@Valid @RequestBody DeleteImageDto deleteDto) {
        uploadService.deleteImage(deleteDto);
        return ResponseEntity.noContent().build();
    }
}
