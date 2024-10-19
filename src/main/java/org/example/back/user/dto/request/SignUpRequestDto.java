package org.example.back.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequestDto(

	@Schema(description = "이메일 주소", example = "lsh0927@naver.com")
	@NotBlank @Email String email,

	@Schema(description = "규격에 맞는 비밀번호", example = "password1234")
	@NotBlank @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,13}$")
	String password,

	@Schema(description = "이메일 인증 코드", example = "1234")
	@NotBlank String certificationNumber

) {
}