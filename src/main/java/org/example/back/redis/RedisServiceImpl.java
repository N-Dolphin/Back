package org.example.back.redis;

import java.util.concurrent.TimeUnit;

import org.example.back.user.exception.RefreshTokenExpiredException;
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
	public void saveRefreshToken(String userId, String refreshToken, long duration) {
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		// refreshToken을 key로, userId를 value로 저장
		operations.set(refreshToken, userId, duration, TimeUnit.MILLISECONDS);
		log.info("Refresh token saved to Redis: token={}, userId={}, duration={}ms", refreshToken, userId, duration);
	}

	@Override
	public String getRefreshToken(String refreshToken) {
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			Object value = operations.get(refreshToken);
			log.info("Redis get operation - Token: {}, Retrieved UserId: {}, Value Type: {}",
				refreshToken, value, value != null ? value.getClass().getName() : "null");
			return (String) value;
		} catch (Exception e) {
			log.error("Redis operation failed for refresh token: " + refreshToken, e);
			throw e;
		}
	}

	public boolean isRefreshTokenExpired(String refreshToken) {
		Long ttl = redisTemplate.getExpire(refreshToken);
		return ttl == null || ttl <= 0;  // 키가 없거나 만료된 경우
	}

	public String validateRefreshToken(String refreshToken) {
		if (isRefreshTokenExpired(refreshToken)) {
			throw new RefreshTokenExpiredException();
		}
		return getRefreshToken(refreshToken);
	}

	@Override
	// RedisService에 메소드 추가
	public void saveOAuthTokens(String userId, String springRefreshToken, String kakaoRefreshToken, long springDuration, long kakaoDuration) {
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		operations.set(userId + ":spring", springRefreshToken, springDuration, TimeUnit.MILLISECONDS);
		operations.set(userId + ":kakao", kakaoRefreshToken, kakaoDuration, TimeUnit.MILLISECONDS);
	}


	@Override
	public boolean setIfAbsent(String key, String value, long timeoutSeconds) {
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		return Boolean.TRUE.equals(operations.setIfAbsent(key, value, timeoutSeconds, TimeUnit.SECONDS));
	}

	@Override
	public void delete(String key) {
		try {
			redisTemplate.delete(key);
			log.info("Key deleted from Redis: {}", key);
		} catch (Exception e) {
			log.error("Failed to delete key from Redis: {}", key, e);
			throw e;
		}
	}

}