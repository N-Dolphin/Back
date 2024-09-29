package org.example.back.redis;

public interface RedisService {
	String getRedis(RedisParam param);
	String getRedisWithCacheManager(RedisParam param);
}