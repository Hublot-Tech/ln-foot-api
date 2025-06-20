package co.hublots.ln_foot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LnFootApplication {

	public static void main(String[] args) {
		SpringApplication.run(LnFootApplication.class, args);
	}

}
