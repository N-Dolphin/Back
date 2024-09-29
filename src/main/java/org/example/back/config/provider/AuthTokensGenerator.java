package org.example.back.config.provider;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthTokensGenerator {
	private static final String BEARER_TYPE = "Bearer";
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;            // 30분
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

	private final JwtTokenProvider jwtTokenProvider;

	public AuthTokens generate(Long memberId) {
		long now = (new Date()).getTime();

		Date accessTokenExpiredAt = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
		Date refreshTokenExpiredAt = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

		String accessToken = jwtTokenProvider.generate(memberId.toString(), accessTokenExpiredAt);

		// 리프레시 토큰은 서버 내부에서만 관리할 수 있도록 별도로 처리
		String refreshToken = UUID.randomUUID().toString(); // 랜덤한 문자열로 생성

		// refreshToken을 Redis 또는 DB에 저장 (예: Redis 사용 시)
		// refreshTokenRepository.save(refreshToken, memberId, refreshTokenExpiredAt);
		return AuthTokens.of(accessToken, refreshToken, BEARER_TYPE, ACCESS_TOKEN_EXPIRE_TIME / 1000L);

	}

	public Long extractMemberId(String accessToken) {
		return Long.valueOf(jwtTokenProvider.extractSubject(accessToken));
	}
}