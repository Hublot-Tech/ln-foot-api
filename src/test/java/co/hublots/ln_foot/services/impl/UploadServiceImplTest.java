package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*; // Assuming this covers most Minio exceptions
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // For setting @Value fields

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceImplTest {

    @Mock
    private MinioClient minioClient;

    private UploadServiceImpl uploadService;

    private final String BUCKET_NAME = "test-bucket";
    private final String MINIO_API_URL = "http://minio.test.com";

    @BeforeEach
    void setUp() {
        uploadService = new UploadServiceImpl(minioClient);
        // Manually set @Value fields for testing
        ReflectionTestUtils.setField(uploadService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(uploadService, "minioApiUrl", MINIO_API_URL);
    }

    @Test
    void getImagePresignedUrl_returnsCorrectResponseDto() throws Exception {
        // Arrange
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder()
                .fileName("test-image.png")
                .contentType("image/png")
                .entityType("team")
                .entityId("team123")
                .contentLength(1024L * 500L) // 500KB
                .build();

        Map<String, String> mockFormData = new HashMap<>();
        mockFormData.put("key", "uploads/images/team/team123/some-uuid-test-image.png");
        mockFormData.put("policy", "base64policy");
        mockFormData.put("X-Amz-Algorithm", "AWS4-HMAC-SHA256");
        // ... other typical form data fields ...

        when(minioClient.getPresignedPostFormData(any(PostPolicy.class))).thenReturn(mockFormData);

        // Act
        ImagePresignedUrlResponseDto response = uploadService.getImagePresignedUrl(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(MINIO_API_URL + "/" + BUCKET_NAME, response.getUploadUrl());
        assertEquals(mockFormData, response.getFormData());
        assertTrue(response.getKey().startsWith("uploads/images/team/team123/"));
        assertTrue(response.getKey().endsWith("-test-image.png"));
        // finalUrl is constructed based on uploadUrl and key
        assertEquals(MINIO_API_URL + "/" + BUCKET_NAME + "/" + response.getKey(), response.getFinalUrl());

        verify(minioClient).getPresignedPostFormData(any(PostPolicy.class));
    }

    @Test
    void getImagePresignedUrl_handlesDefaultContentTypeAndSize() throws Exception {
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder()
                .fileName("default.jpg")
                // No contentType or contentLength
                .build();

        Map<String, String> mockFormData = new HashMap<>();
        mockFormData.put("key", "uploads/images/some-uuid-default.jpg");

        when(minioClient.getPresignedPostFormData(any(PostPolicy.class))).thenReturn(mockFormData);

        uploadService.getImagePresignedUrl(requestDto);

        // Verification of PostPolicy conditions (contentType startsWith "image/", default size range)
        // would require capturing the PostPolicy argument or more complex mocking.
        // For now, ensuring the method runs and calls getPresignedPostFormData is the primary goal.
        verify(minioClient).getPresignedPostFormData(any(PostPolicy.class));
    }


    @Test
    void getImagePresignedUrl_whenMinioThrowsException_propagatesAsRuntimeException() throws Exception {
        // Arrange
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder().fileName("test.png").build();
        when(minioClient.getPresignedPostFormData(any(PostPolicy.class)))
                .thenThrow(new ServerException("Minio server error", null, null));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            uploadService.getImagePresignedUrl(requestDto);
        });
        assertTrue(thrown.getMessage().contains("Error generating presigned URL"));
    }

    @Test
    void deleteImage_callsMinioRemoveObject() throws Exception {
        // Arrange
        String objectKey = "uploads/images/team/team123/some-uuid-test-image.png";
        DeleteImageDto deleteDto = DeleteImageDto.builder().key(objectKey).build();

        // Act
        uploadService.deleteImage(deleteDto);

        // Assert
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());
        assertEquals(BUCKET_NAME, captor.getValue().bucket());
        assertEquals(objectKey, captor.getValue().object());
    }

    @Test
    void deleteImage_keyNullOrBlank_throwsIllegalArgumentException() {
        DeleteImageDto deleteDtoNoKey = DeleteImageDto.builder().key(null).build();
        DeleteImageDto deleteDtoBlankKey = DeleteImageDto.builder().key("  ").build();

        assertThrows(IllegalArgumentException.class, () -> uploadService.deleteImage(deleteDtoNoKey));
        assertThrows(IllegalArgumentException.class, () -> uploadService.deleteImage(deleteDtoBlankKey));
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }


    @Test
    void deleteImage_whenMinioThrowsException_propagatesAsRuntimeException() throws Exception {
        // Arrange
        String objectKey = "valid-key.png";
        DeleteImageDto deleteDto = DeleteImageDto.builder().key(objectKey).build();
        doThrow(new ServerException("Minio server error on delete", null, null))
            .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            uploadService.deleteImage(deleteDto);
        });
        assertTrue(thrown.getMessage().contains("Failed to delete image from Minio"));
    }
}
