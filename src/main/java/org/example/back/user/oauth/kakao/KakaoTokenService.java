package org.example.back.user.oauth.kakao;

import org.example.back.redis.RedisService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoTokenService {
	private final RedisService redisService;

	public void saveKakaoToken(Long userId, KakaoTokens kakaoTokens) {
		redisService.saveRefreshToken(
			"KAKAO:" + userId,
			kakaoTokens.getRefreshToken(),
			Long.parseLong(kakaoTokens.getRefreshTokenExpiresIn()) * 1000
		);
	}
}