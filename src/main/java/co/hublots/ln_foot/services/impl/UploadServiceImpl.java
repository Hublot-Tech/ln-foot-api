package co.hublots.ln_foot.services.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;
import co.hublots.ln_foot.services.UploadService;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);
    private final MinioClient minioClient;

    private static final long DEFAULT_MIN_UPLOAD_SIZE = 1024; // 1KB
    private static final long DEFAULT_MAX_UPLOAD_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif");
    private static final Map<String, List<String>> CONTENT_TYPE_TO_EXTENSIONS_MAP = Map.of(
            "image/jpeg", List.of(".jpg", ".jpeg"),
            "image/png", List.of(".png"),
            "image/gif", List.of(".gif"));

    @Value("${minio.url}") // This is the Minio API endpoint e.g. http://localhost:9000
    private String minioApiUrl;

    private String sanitizePathSegment(String segment) {
        if (!StringUtils.hasText(segment)) {
            return "";
        }
        return segment.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private String sanitizeAndValidateFilename(String originalFilename, String validatedContentType) {
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("Filename cannot be empty.");
        }
        // Basic sanitization: replace unwanted characters, but keep dot for extension.
        // Allow common filename characters: letters, numbers, dot, hyphen, underscore.
        String sanitized = originalFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        // Replace multiple dots with one, ensure it doesn't start/end with dot
        // (simplified for this context)
        sanitized = sanitized.replaceAll("\\.+", ".").replaceAll("^\\.|\\.$", "");

        if (!StringUtils.hasText(sanitized) || sanitized.equals(".")) {
            throw new IllegalArgumentException("Sanitized filename is empty or invalid: " + originalFilename);
        }

        String lowerSanitized = sanitized.toLowerCase();
        int lastDotIndex = lowerSanitized.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex >= lowerSanitized.length() - 1) {
            throw new IllegalArgumentException("Filename is missing a valid extension: " + sanitized);
        }
        String extension = lowerSanitized.substring(lastDotIndex);

        List<String> allowedExtensions = CONTENT_TYPE_TO_EXTENSIONS_MAP.get(validatedContentType.toLowerCase());
        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            String message = String.format(
                    "File extension '%s' is not allowed for content type '%s'. Allowed extensions are: %s",
                    extension, validatedContentType,
                    allowedExtensions != null ? String.join(", ", allowedExtensions) : "none");
            throw new IllegalArgumentException(message);
        }
        return sanitized;
    }

    @Override
    public ImagePresignedUrlResponseDto getImagePresignedUrl(ImagePresignedUrlRequestDto requestDto) {
        if (requestDto.getEntityType() == null || requestDto.getEntityType().isBlank()) {
            log.warn("Entity type must be provided for presigned URL generation.");
            throw new IllegalArgumentException("Entity type must be provided.");
        }
        String bucketName = requestDto.getEntityType();

        String requestedContentType = requestDto.getContentType();
        if (!StringUtils.hasText(requestedContentType)
                || !ALLOWED_CONTENT_TYPES.contains(requestedContentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid or unsupported file type: " + requestedContentType +
                    ". Allowed types are: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }
        String validatedContentType = requestedContentType.toLowerCase();

        String finalFilename = sanitizeAndValidateFilename(requestDto.getFileName(), validatedContentType);

        try {
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                log.info("Bucket {} already exists, proceeding with presigned URL generation.", bucketName);
            } else {
                log.info("Bucket {} does not exist, creating it now.", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String publicReadPolicy = String.format("""
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": "*",
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """, bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(publicReadPolicy)
                            .build());

            String entityTypePath = StringUtils.hasText(bucketName)
                    ? sanitizePathSegment(bucketName) + "/"
                    : "";
            String entityIdPath = StringUtils.hasText(requestDto.getEntityId())
                    ? sanitizePathSegment(requestDto.getEntityId()) + "/"
                    : "";

            String objectKey = (entityTypePath != "" ? entityTypePath
                    : "uploads/images/") +
                    entityIdPath +
                    UUID.randomUUID().toString() + "-" + finalFilename;

            PostPolicy policy = new PostPolicy(bucketName, ZonedDateTime.now().plusMinutes(15));
            policy.addEqualsCondition("key", objectKey);
            policy.addEqualsCondition("Content-Type", validatedContentType);
            policy.addContentLengthRangeCondition(DEFAULT_MIN_UPLOAD_SIZE, DEFAULT_MAX_UPLOAD_SIZE);

            String postUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .method(Method.PUT)
                            .expiry(15 * 60)
                            .build());

            String finalUrl = minioApiUrl + "/" + objectKey;

            return ImagePresignedUrlResponseDto.builder()
                    .uploadUrl(postUrl)
                    .key(objectKey)
                    .finalUrl(finalUrl)
                    .build();

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException
                | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException
                | XmlParserException e) {
            log.error("Error generating presigned POST URL for Minio: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating presigned URL: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(DeleteImageDto deleteDto) {
        if (deleteDto.getKey() == null || deleteDto.getKey().isBlank()) {
            log.warn("Attempted to delete image with null or blank key.");
            throw new IllegalArgumentException("Object key must be provided for deletion.");
        }

        String bucketName = deleteDto.getBucketName() != null ? deleteDto.getBucketName() : "uploads";
        if (!StringUtils.hasText(bucketName)) {
            log.warn("Bucket name must be provided for deletion.");
            throw new IllegalArgumentException("Bucket name must be provided for deletion.");
        }

        log.info("Attempting to delete image with key: {} from bucket: {}", deleteDto.getKey(), bucketName);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(deleteDto.getKey())
                            .build());
            log.info("Successfully deleted image with key: {}", deleteDto.getKey());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException
                | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException
                | XmlParserException e) {
            log.error("Failed to delete image with key {}: {}", deleteDto.getKey(), e.getMessage(), e);
            // Depending on policy, might not want to expose detailed error to client,
            // but for service layer, rethrow as runtime or custom service exception.
            throw new RuntimeException(
                    "Failed to delete image from Minio: " + deleteDto.getKey() + "; " + e.getMessage(), e);
        }
    }
}
