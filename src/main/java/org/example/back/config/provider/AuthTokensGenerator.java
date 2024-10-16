package org.example.back.config.provider;

import java.util.Date;
import java.util.UUID;

import org.example.back.redis.RedisService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthTokensGenerator {
	private static final String BEARER_TYPE = "Bearer";
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 120;            // 120분
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

	private final JwtTokenProvider jwtTokenProvider;
	private final RedisService redisService;

	public AuthTokens generate(Long memberId) {
		long now = (new Date()).getTime();

		Date accessTokenExpiredAt = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
		Date refreshTokenExpiredAt = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

		String subject = memberId.toString();
		String accessToken = jwtTokenProvider.generate(memberId.toString(), accessTokenExpiredAt);
		String refreshToken = UUID.randomUUID().toString(); // 랜덤한 문자열로 생성


		redisService.saveRefreshToken(subject,refreshToken,REFRESH_TOKEN_EXPIRE_TIME);

		return AuthTokens.of(accessToken, refreshToken, BEARER_TYPE, ACCESS_TOKEN_EXPIRE_TIME / 1000L);

	}

	public Long extractMemberId(String accessToken) {
		return Long.valueOf(jwtTokenProvider.extractSubject(accessToken));
	}
}