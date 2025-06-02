package co.hublots.ln_foot.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.MakeBucketArgs;
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

    public String uploadFile(String bucketName, MultipartFile file) {
        try {
            boolean doesBucketExits = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!doesBucketExits) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

                String publicReadPolicy = "{\n"
                        + "    \"Version\": \"2012-10-17\",\n"
                        + "    \"Statement\": [\n"
                        + "        {\n"
                        + "            \"Effect\": \"Allow\",\n"
                        + "            \"Principal\": {\n"
                        + "                \"AWS\": [\"*\"]\n"
                        + "            },\n"
                        + "            \"Action\": [\"s3:GetObject\"],\n"
                        + "            \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}";
                minioClient
                        .setBucketPolicy(
                                SetBucketPolicyArgs.builder().bucket(bucketName).config(publicReadPolicy).build());
            } else {
                log.info("Bucket %s already exists.", bucketName);
            }

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
