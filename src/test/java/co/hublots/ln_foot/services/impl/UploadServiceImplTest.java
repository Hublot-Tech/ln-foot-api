package co.hublots.ln_foot.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

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
        ReflectionTestUtils.setField(uploadService, "minioApiUrl", MINIO_API_URL);
    }

    @Test
    void getImagePresignedUrl_returnsCorrectResponseDto() throws Exception {
        // Arrange
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder()
                .fileName("test-image.png")
                .contentType("image/png")
                .entityType("test-bucket")
                .entityId("team123")
                .contentLength(1024L * 500L) // 500KB
                .build();

        String mockUploadUrl = MINIO_API_URL + "/test-bucket/team123/some-uuid-test-image.png";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(mockUploadUrl);

        // Act
        ImagePresignedUrlResponseDto response = uploadService.getImagePresignedUrl(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(mockUploadUrl, response.getUploadUrl());
        assertTrue(response.getKey().startsWith("test-bucket/team123/"));
        assertTrue(response.getKey().endsWith("-test-image.png"));

        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void getImagePresignedUrl_handlesDefaultContentTypeAndSize() throws Exception {
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder()
                .fileName("default.jpg")
                .entityType(BUCKET_NAME)
                .contentType("image/jpeg") // Default content type
                // No contentType or contentLength
                .build();

        String mockUploadUrl = MINIO_API_URL + "/uploads/images/some-uuid-default.jpg";
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(mockUploadUrl);

        uploadService.getImagePresignedUrl(requestDto);

        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void getImagePresignedUrl_whenMinioThrowsException_propagatesAsRuntimeException() throws Exception {
        // Arrange
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder()
                .contentType("image/png")
                .entityType(BUCKET_NAME)
                .fileName("test.png")
                .build();
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new ServerException("Minio server error", 500, null));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            uploadService.getImagePresignedUrl(requestDto);
        });
        assertTrue(thrown.getMessage().contains("Error generating presigned URL"));
    }

    @Test
    void deleteImage_callsMinioRemoveObject() throws Exception {
        // Arrange
        String objectKey = "uploads/images/test-bucket/team123/some-uuid-test-image.png";
        DeleteImageDto deleteDto = DeleteImageDto.builder().bucketName(BUCKET_NAME).key(objectKey).build();

        // Act
        uploadService.deleteImage(deleteDto);

        // Assert
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());
        assertEquals(BUCKET_NAME, captor.getValue().bucket());
        assertEquals(objectKey, captor.getValue().object());
    }

    @Test
    void deleteImage_keyNullOrBlank_throwsIllegalArgumentException()
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IOException {
        DeleteImageDto deleteDtoNoKey = DeleteImageDto.builder().bucketName(BUCKET_NAME).key(null).build();
        DeleteImageDto deleteDtoBlankKey = DeleteImageDto.builder().bucketName(BUCKET_NAME).key("  ").build();

        assertThrows(IllegalArgumentException.class, () -> uploadService.deleteImage(deleteDtoNoKey));
        assertThrows(IllegalArgumentException.class, () -> uploadService.deleteImage(deleteDtoBlankKey));
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteImage_whenMinioThrowsException_propagatesAsRuntimeException() throws Exception {
        // Arrange
        String objectKey = "valid-key.png";
        DeleteImageDto deleteDto = DeleteImageDto.builder().bucketName(BUCKET_NAME)
                .key(objectKey).build();
        doThrow(new ServerException("Minio server error on delete", 500, null))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            uploadService.deleteImage(deleteDto);
        });
        assertTrue(thrown.getMessage().contains("Failed to delete image from Minio"));
    }
}
