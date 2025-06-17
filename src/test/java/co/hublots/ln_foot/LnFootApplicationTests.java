package co.hublots.ln_foot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import io.minio.MinioClient;

@SpringBootTest
@ActiveProfiles("test")
class LnFootApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class SecurityTestConfig {
		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> Jwt.withTokenValue(token)
					.header("alg", "none")
					.claim("sub", "test")
					.build();
		}

		@Bean
		public MinioClient minioClient() {
			return MinioClient.builder()
					.endpoint("http://localhost:9000")
					.credentials("accessKey", "secretKey")
					.build();
		}

		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}

}
