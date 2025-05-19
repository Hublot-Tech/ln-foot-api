package co.hublots.ln_foot.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename().replaceAll(" ", "");
            String uniqueName = UUID.randomUUID() + "-" + originalFilename;

            Path tempFile = Files.createTempFile("upload-", "-" + originalFilename);
            file.transferTo(tempFile.toFile());

            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueName)
                            .filename(tempFile.toString())
                            .contentType(file.getContentType())
                            .build());

            Files.deleteIfExists(tempFile);

            String url = Paths.get(minioUrl, bucketName, uniqueName).toString();

            log.info("Upload complete: {}", url);
            return url;

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RuntimeException("File upload failed", e);
        }
    }
}
