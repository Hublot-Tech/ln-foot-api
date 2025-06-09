package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;
import co.hublots.ln_foot.services.UploadService;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.url}") // This is the Minio API endpoint e.g. http://localhost:9000
    private String minioApiUrl;


    private String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        return filename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }

    @Override
    public ImagePresignedUrlResponseDto getImagePresignedUrl(ImagePresignedUrlRequestDto requestDto) {
        try {
            String objectKey = "uploads/images/" +
                               (requestDto.getEntityType() != null ? sanitizeFilename(requestDto.getEntityType()) + "/" : "") +
                               (requestDto.getEntityId() != null ? sanitizeFilename(requestDto.getEntityId()) + "/" : "") +
                               UUID.randomUUID().toString() + "-" + sanitizeFilename(requestDto.getFileName());

            PostPolicy policy = new PostPolicy(bucketName, ZonedDateTime.now().plusMinutes(15)); // Expires in 15 minutes
            policy.addEqualsCondition("key", objectKey);

            if (requestDto.getContentType() != null && !requestDto.getContentType().isBlank()) {
                policy.addEqualsCondition("Content-Type", requestDto.getContentType());
            } else {
                policy.addStartsWithCondition("Content-Type", "image/"); // Default to image/*
            }

            long minSize = requestDto.getContentLength() != null && requestDto.getContentLength() > 0 ? Math.min(1024, requestDto.getContentLength()) : 1024; // Min 1KB or actual if smaller
            long maxSize = requestDto.getContentLength() != null && requestDto.getContentLength() > 0 ? Math.max(10 * 1024 * 1024, requestDto.getContentLength()) : 10 * 1024 * 1024; // Max 10MB or actual if larger
            policy.addContentLengthRangeCondition(minSize, maxSize);


            Map<String, String> formData = minioClient.getPresignedPostFormData(policy);

            // The URL to POST to is the Minio server's bucket URL.
            // minioApiUrl is like http://localhost:9000. The form action should be http://localhost:9000/bucketName
            String postUrl = minioApiUrl + (minioApiUrl.endsWith("/") ? "" : "/") + bucketName;

            // Construct a potential final URL. This might vary based on CDN or public access setup.
            // Assuming direct access through Minio URL for now if objects are public.
            String finalUrl = postUrl + "/" + objectKey;


            return ImagePresignedUrlResponseDto.builder()
                    .uploadUrl(postUrl)
                    .formData(formData)
                    .key(objectKey) // The key is also part of formData, but useful to return separately
                    .finalUrl(finalUrl) // This is a best guess for the final direct URL
                    .build();

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("Error generating presigned POST URL for Minio: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating presigned URL: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(DeleteImageDto deleteDto) {
        if (deleteDto.getKey() == null || deleteDto.getKey().isBlank()) {
            log.warn("Attempted to delete image with null or blank key.");
            // Optionally parse key from deleteDto.getImageUrl() if that's a supported pattern
            // For now, require key.
            throw new IllegalArgumentException("Object key must be provided for deletion.");
        }

        log.info("Attempting to delete image with key: {} from bucket: {}", deleteDto.getKey(), bucketName);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(deleteDto.getKey())
                            .build());
            log.info("Successfully deleted image with key: {}", deleteDto.getKey());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("Failed to delete image with key {}: {}", deleteDto.getKey(), e.getMessage(), e);
            // Depending on policy, might not want to expose detailed error to client,
            // but for service layer, rethrow as runtime or custom service exception.
            throw new RuntimeException("Failed to delete image from Minio: " + deleteDto.getKey() + "; " + e.getMessage(), e);
        }
    }
}
