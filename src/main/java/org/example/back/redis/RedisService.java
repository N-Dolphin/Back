package org.example.back.redis;

public interface RedisService {
	String getRedis(RedisParam param);
	String getRedisWithCacheManager(RedisParam param);

	void saveRefreshToken(String key, String refreshToken, long duration);

	// 추가
	String getRefreshToken(String key);
	String validateRefreshToken(String refreshToken);
	void saveOAuthTokens(String userId, String springRefreshToken, String kakaoRefreshToken, long springDuration, long kakaoDuration);

	boolean setIfAbsent(String key, String value, long timeoutSeconds);
	void delete(String key);
}