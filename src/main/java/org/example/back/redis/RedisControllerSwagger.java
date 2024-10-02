package org.example.back.redis;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface RedisControllerSwagger {

	@Operation(summary = "Redis 데이터 가져오기", description = "Redis에서 주어진 key에 대한 데이터를 가져옵니다. 만약 해당 key가 존재하지 않으면, 새로운 value를 저장합니다.")
	@ApiResponse(responseCode = "200", description = "Redis에서 데이터를 성공적으로 가져왔습니다.")
	ResponseEntity<String> getRedis(RedisParam param);

	@Operation(summary = "CacheManager를 사용한 Redis 데이터 가져오기", description = "CacheManager를 통해 Redis에서 주어진 key에 대한 데이터를 캐시를 이용해 가져옵니다.")
	@ApiResponse(responseCode = "200", description = "CacheManager를 사용하여 데이터를 성공적으로 가져왔습니다.")
	ResponseEntity<String> getRedisWithCacheManager(RedisParam param);

	@Operation(summary = "Redis 연결 테스트", description = "Redis 연결이 성공했는지 테스트합니다.")
	@ApiResponse(responseCode = "200", description = "Redis 연결이 성공적입니다.")
	ResponseEntity<String> testRedisConnection();
}
