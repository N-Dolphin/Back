package org.example.back.healthCheck;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.TreeMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

	@Value("${server.env}")
	private String env;

	@Value("${server.port}")
	private String serverPort;

	@Value("${server.serverAddress}")
	private String serverAddress;

	@Value("${serverName}")
	private String serverName;

	@GetMapping("/hc")
	public ResponseEntity<String> healthCheck() {
		try {
			log.info("Health check called. env: {}", env);  // 로그 추가
			return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(env);
		} catch (Exception e) {
			log.error("Health check failed", e);  // 상세 에러 로그
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Error: " + e.getMessage());
		}
	}

	@GetMapping("/env")
	public ResponseEntity<?> getEnv() {
		return ResponseEntity.ok(env);

	}

}