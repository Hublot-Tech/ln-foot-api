package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class UploadServiceImplTest {
    private UploadServiceImpl uploadService;

    @BeforeEach
    void setUp() {
        // If MinioService was a constructor arg, it would be mocked here.
        // But for the current mock, it's not.
        uploadService = new UploadServiceImpl();
    }

    @Test
    void getImagePresignedUrl_returnsMockResponse() {
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder()
                .fileName("test-image.png")
                .contentType("image/png")
                .entityType("team")
                .entityId("team123")
                .build();

        ImagePresignedUrlResponseDto response = uploadService.getImagePresignedUrl(requestDto);

        assertNotNull(response);
        assertNotNull(response.getUploadUrl());
        assertNotNull(response.getKey());
        assertNotNull(response.getFinalUrl());
        assertTrue(response.getKey().contains("team/team123"));
        assertTrue(response.getKey().contains("test-image.png"));
        assertTrue(response.getUploadUrl().startsWith("http://mock-s3-bucket.example.com/"));
        assertTrue(response.getFinalUrl().startsWith("http://cdn.example.com/"));
    }

    @Test
    void getImagePresignedUrl_handlesNullEntityFields() {
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder()
                .fileName("another-image.jpg")
                .contentType("image/jpeg")
                // entityType and entityId are null
                .build();

        ImagePresignedUrlResponseDto response = uploadService.getImagePresignedUrl(requestDto);

        assertNotNull(response);
        assertNotNull(response.getKey());
        assertFalse(response.getKey().startsWith("null/")); // Check it doesn't literally include "null"
        assertTrue(response.getKey().contains("another-image.jpg"));
    }

    @Test
    void deleteImage_completesWithoutError_withKey() {
        DeleteImageDto deleteDto = DeleteImageDto.builder().key("some/s3/key.png").build();
        // Mock method is void and just logs
        assertDoesNotThrow(() -> uploadService.deleteImage(deleteDto));
        // To verify logging, would need a logging appender test setup, which is more complex.
        // For this scope, just ensuring it runs is sufficient for the mock.
    }

    @Test
    void deleteImage_completesWithoutError_withImageUrl() {
        DeleteImageDto deleteDto = DeleteImageDto.builder().imageUrl("http://example.com/some/s3/key.png").build();
        assertDoesNotThrow(() -> uploadService.deleteImage(deleteDto));
    }
}
