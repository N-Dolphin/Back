package org.example.back.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedisController implements RedisControllerSwagger {

	private final RedisService redisService;
	private final RedisTemplate<String, String> redisTemplate;

	@GetMapping("/redis")
	@Override
	public ResponseEntity<String> getRedis(@RequestBody RedisParam param) {
		String result = redisService.getRedis(param);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/manager")
	@Override
	public ResponseEntity<String> getRedisWithCacheManager(@RequestBody RedisParam param) {
		String result = redisService.getRedisWithCacheManager(param);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/test-redis")
	@Override
	public ResponseEntity<String> testRedisConnection() {
		try {
			redisTemplate.getConnectionFactory().getConnection().ping();
			return ResponseEntity.ok("Redis connection successful");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Failed to connect to Redis: " + e.getMessage());
		}
	}
}



