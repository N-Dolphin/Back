package org.example.back.healthCheck;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

	@GetMapping("/hc")
	public ResponseEntity<String> healthCheck() {
		try {
			// 가장 단순한 응답만 반환
			return ResponseEntity.ok("UP");
		} catch (Exception e) {
			log.error("Health check failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DOWN");
		}
	}
}