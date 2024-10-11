package org.example.back.user.controller;

import java.io.IOException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.example.back.config.provider.AuthTokens;
import org.example.back.user.oauth.kakao.KakaoLoginParams;
import org.springframework.http.ResponseEntity;

public interface AuthControllerSwagger {

	@Operation(summary = "카카오 로그인", description = "카카오 로그인을 처리하는 API입니다.")
	@ApiResponse(responseCode = "200", description = "로그인에 성공했습니다.")
	ResponseEntity<?> loginKakao(KakaoLoginParams params);

	@Operation(summary = "카카오 로그인 리다이렉트", description = "카카오 로그인 페이지로 리다이렉트합니다.")
	@ApiResponse(responseCode = "302", description = "카카오 로그인 페이지로 리다이렉트됩니다.")
	void redirectToKakaoLogin(HttpServletResponse response) throws IOException;
}
