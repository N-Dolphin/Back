package org.example.back.redis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedisController {

	private final RedisService redisService;

	@GetMapping("/redis")
	public String getRedis(@RequestBody RedisParam param) {
		return redisService.getRedis(param);
	}
	@GetMapping("/manager")
	public String getRedisWithCacheManager(@RequestBody RedisParam param) {
		return redisService.getRedisWithCacheManager(param);
	}
}
