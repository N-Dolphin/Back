package org.example.back.user.controller;

import java.io.IOException;

import org.example.back.config.provider.AuthTokens;
import org.example.back.redis.RedisService;
import org.example.back.redis.RedisServiceImpl;
import org.example.back.user.oauth.kakao.KakaoLoginParams;
import org.example.back.user.service.OAuthLoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "카카오 OAuth 로그인 관련 API")
public final class AuthController implements AuthControllerSwagger{
	private final OAuthLoginService oAuthLoginService;
	private final RedisServiceImpl redisService;

	@Value("${oauth.kakao.client-id}")
	private String CLIENT_ID;


	@Value("${oauth.kakao.redirectUrl}")
	private String REDIRECT_URI;

	@Value("${oauth.kakao.authorizeUrl}")
	private String AUTHORIZATION_ENDPOINT;


	// @PostMapping("/kakao")
	// @Override
	// public ResponseEntity<?> loginKakao(@RequestBody KakaoLoginParams params) {
	// 	try {
	// 		return ResponseEntity.ok(oAuthLoginService.login(params));
	// 	} catch (HttpClientErrorException.BadRequest e) {
	// 		return ResponseEntity.badRequest().body("인증 코드가 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.");
	// 	}
	// }
	// AuthController에 동시성 제어 추가
	@PostMapping("/kakao")
	public ResponseEntity<?> loginKakao(@RequestBody KakaoLoginParams params) {
		String requestKey = "kakao:auth:" + params.getAuthorizationCode();
		if (!redisService.setIfAbsent(requestKey, "processing", 30)) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("처리 중입니다");
		}
		try {
			return ResponseEntity.ok(oAuthLoginService.login(params));
		} catch (HttpClientErrorException.BadRequest e) {
			return ResponseEntity.badRequest().body("인증 코드가 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.");
		} finally {
			redisService.delete(requestKey);
		}
	}
	

	@GetMapping
	@Override
	public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
		String redirectUrl = String.format(
			"%s?client_id=%s&redirect_uri=%s&response_type=code&prompt=login",
			AUTHORIZATION_ENDPOINT,
			CLIENT_ID,
			REDIRECT_URI
		);
		response.sendRedirect(redirectUrl);
	}

}



