package co.hublots.ln_foot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        log.debug("Minio Access Key: " + accessKey + " Secret Key:" + secretKey);

        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        return minioClient;
    }
}
