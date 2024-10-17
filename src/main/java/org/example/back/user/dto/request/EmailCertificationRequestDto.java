package org.example.back.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증 요청")
public record EmailCertificationRequestDto(

	@Email(message = "email should be valid")
	@Schema(description = "이메일 인증을 위한 이메일 주소", example = "lsheon0927@naver.com")
	@NotBlank @Email String email
) {
}



