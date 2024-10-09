package org.example.back.user.dto.response;

import org.example.back.config.provider.AuthTokens;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignInResponseDto(

	@Schema(description = "엑세스토큰", example = "QWERASDFZXVTYUIGHJKBNM")
	AuthTokens token,

	@Schema(description = "만료시간")
	Long expiredTime,

	@Schema(description = "프로필 생성 여부")
	boolean hasProfile
	) {
}

