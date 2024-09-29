package org.example.back.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedisController {

	private final RedisService redisService;

	private final RedisTemplate<String, String> redisTemplate;

	@GetMapping("/redis")
	public String getRedis(@RequestBody RedisParam param) {
		return redisService.getRedis(param);
	}

	@GetMapping("/manager")
	public String getRedisWithCacheManager(@RequestBody RedisParam param) {
		return redisService.getRedisWithCacheManager(param);
	}

	@GetMapping("/test-redis")
	public String testRedisConnection() {
		try {
			redisTemplate.getConnectionFactory().getConnection().ping();
			return "Redis connection successful";
		} catch (Exception e) {
			return "Failed to connect to Redis: " + e.getMessage();
		}
	}

}