package org.example.back.user.controller;

import org.example.back.config.provider.AuthTokens;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RequestHeader;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Token", description = "토큰 갱신 관련 API")
public interface RefreshTokenControllerSwagger {

	@Operation(
		summary = "Access Token 갱신",
		description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "토큰 갱신 성공",
			content = @Content(schema = @Schema(implementation = AuthTokens.class))
		),
		@ApiResponse(
			responseCode = "401",
			description = "유효하지 않은 Refresh Token",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	ResponseEntity<AuthTokens> refreshAccessToken(
		@Parameter(
			description = "HttpServletRequest with Authorization header containing Refresh Token",
			required = true
		)
		HttpServletRequest request
	);
}