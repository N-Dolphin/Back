package org.example.back.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Schema(description = "이메일 인증 응답")
public record EmailCertificationResponseDto(

        @Schema(description = "성공 여부", example = "성공!")
        String status,

        @Schema(description = "성공 확인 or 실패 원인 ", example = "이미 인증이 완료된 이메일입니다")
        String message
) {}