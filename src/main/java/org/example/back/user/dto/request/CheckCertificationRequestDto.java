package org.example.back.user.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CheckCertificationRequestDto (

        @Schema(description = "이메일 인증을 위한 유저 이름", example = "lsh0927")
        @NotBlank String id,

        @Schema(description = "이메일", example = "lsh0927@naver.com")
        @Email @NotBlank String email,

        @Schema(description = "이메일로 전송된 인증 코드", example = "1234")
        @NotBlank String certificationNumber
) {}