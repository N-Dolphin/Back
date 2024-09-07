package org.example.back.user.controller;

import java.io.IOException;

import org.example.back.config.provider.AuthTokens;
import org.example.back.user.oauth.kakao.KakaoLoginParams;
import org.example.back.user.service.OAuthLoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "카카오 OAuth 로그인 관련 API")
public final class AuthController implements AuthControllerSwagger{
	private final OAuthLoginService oAuthLoginService;

	@Value("${oauth.kakao.client-id}")
	private String CLIENT_ID;


	@Value("${oauth.kakao.redirectUrl}")
	private String REDIRECT_URI;

	@Value("${oauth.kakao.authorizeUrl}")
	private String AUTHORIZATION_ENDPOINT;


	@PostMapping("/kakao")
	@Override
	public ResponseEntity<AuthTokens> loginKakao(@RequestBody String code) {
		KakaoLoginParams kakaoLoginParams= new KakaoLoginParams();
		kakaoLoginParams.setAuthorizationCode(code);
		return ResponseEntity.ok(oAuthLoginService.login(kakaoLoginParams));
	}


	@GetMapping
	@Override
	public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
		String redirectUrl = String.format(
			"%s?client_id=%s&redirect_uri=%s&response_type=code",
			AUTHORIZATION_ENDPOINT,
			CLIENT_ID,
			REDIRECT_URI
		);
		response.sendRedirect(redirectUrl);
	}
}



