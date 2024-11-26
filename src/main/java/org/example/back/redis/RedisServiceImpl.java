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

	@Override
	public String getRefreshToken(String key) {
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			Object value = operations.get(key);
			log.info("Redis get operation - Key: {}, Retrieved Value: {}, Value Type: {}",
				key, value, value != null ? value.getClass().getName() : "null");
			return (String) value;
		} catch (Exception e) {
			log.error("Redis operation failed for key: " + key, e);
			throw e;
		}
	}


	// RedisService에 메소드 추가
	public void saveOAuthTokens(String userId, String springRefreshToken, String kakaoRefreshToken, long springDuration, long kakaoDuration) {
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		operations.set(userId + ":spring", springRefreshToken, springDuration, TimeUnit.MILLISECONDS);
		operations.set(userId + ":kakao", kakaoRefreshToken, kakaoDuration, TimeUnit.MILLISECONDS);
	}

}