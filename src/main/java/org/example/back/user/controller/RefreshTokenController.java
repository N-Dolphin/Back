package org.example.back.user.controller;

import static org.example.back.config.provider.AuthTokensGenerator.*;

import java.util.Date;
import java.util.UUID;

import org.example.back.config.provider.AuthTokens;
import org.example.back.config.provider.AuthTokensGenerator;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.redis.RedisService;
import org.example.back.user.dto.request.RefreshTokenRequest;
import org.example.back.user.exception.InvalidTokenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
public class RefreshTokenController implements RefreshTokenControllerSwagger{

	private final AuthTokensGenerator authTokensGenerator;
	private final RedisService redisService;
	private final JwtTokenProvider jwtTokenProvider;

	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 120;            // 120분
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

	@PostMapping("/refresh")
	@Override
	public ResponseEntity<AuthTokens> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
		try {
			String refreshToken = request.getRefreshToken();

			String userId = redisService.getRefreshToken(refreshToken);
			System.out.println("유저아이디는:" + userId);

			// 새로운 Access Token 생성
			Date accessTokenExpiredAt = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME);
			String newAccessToken = jwtTokenProvider.generate(userId, accessTokenExpiredAt);

			// 새로운 Refresh Token 생성
			String newRefreshToken = UUID.randomUUID().toString();

			// Redis 업데이트
			redisService.saveRefreshToken(userId, newRefreshToken, REFRESH_TOKEN_EXPIRE_TIME);

			return ResponseEntity.ok(AuthTokens.of(
				newAccessToken,
				newRefreshToken,  // 새로운 refresh token
				"Bearer",
				ACCESS_TOKEN_EXPIRE_TIME / 1000L
			));
		} catch (Exception e) {
			throw new InvalidTokenException("Failed to refresh token: " + e.getMessage());
		}
	}
	// 토큰 추출 메소드
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}