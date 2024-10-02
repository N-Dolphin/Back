package org.example.back.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	public String getRedis(RedisParam param) {
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		String result = (String) operations.get(param.key());
		if (!StringUtils.hasText(result)) {
			operations.set(param.key(), param.value(), 10, TimeUnit.MINUTES);
			// log.info("redis save");
			result = param.value();
		}
		return result;
	}

	@Override
	@Cacheable(value = "getRedisWithCacheManager", key = "#param.key", cacheManager = "redisCacheManager")
	public String getRedisWithCacheManager(RedisParam param) {
		return param.value();
	}


	@Override
	public void saveRefreshToken(String key, String refreshToken, long duration) {
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		operations.set(key, refreshToken, duration, TimeUnit.MILLISECONDS);
		log.info("Refresh token saved to Redis: key={}, duration={}ms", key, duration);
	}

}