package org.example.back.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckCertificationResponseDto(

	@Schema(description = "성공 여부", example = "성공!")
	String status,

	@Schema(description = "성공 여부", example = "성공시 메세지")
	String message

) {
}