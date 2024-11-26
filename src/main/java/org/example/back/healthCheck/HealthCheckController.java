package org.example.back.healthCheck;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

	@Value("${server.env}")
	private String env;

	@PostConstruct
	public void init() {
		log.info("HealthCheckController initialized with env: {}", env);
	}

	@GetMapping("/hc")
	public ResponseEntity<String> healthCheck() {
		try {
			log.info("Health check called. Current env value: {}", env);
			if (env == null || env.isEmpty()) {
				log.error("Environment value is null or empty");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Environment not properly configured");
			}
			return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(env);
		} catch (Exception e) {
			log.error("Health check failed with error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Error: " + e.getMessage());
		}
	}
}