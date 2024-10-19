package org.example.back.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SignInRequestDto(

	@Schema(description = "로그인할 이메일", example = "lsheon0927@naver.com")
	@NotBlank String email,

	@Schema(description = "로그인할 패스워드", example = "password1234")
	@NotBlank String password
) {
}
