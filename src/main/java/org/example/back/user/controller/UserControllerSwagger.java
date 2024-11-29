package org.example.back.user.controller;

import org.example.back.exception.ClientErrorResponse;
import org.example.back.user.dto.User;
import org.example.back.user.dto.request.CheckCertificationRequestDto;
import org.example.back.user.dto.request.EmailCertificationRequestDto;
import org.example.back.user.dto.request.SignInRequestDto;
import org.example.back.user.dto.request.SignUpRequestDto;
import org.example.back.user.dto.response.CheckCertificationResponseDto;
import org.example.back.user.dto.response.EmailCertificationResponseDto;
import org.example.back.user.dto.response.SignInResponseDto;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface UserControllerSwagger {

	@Operation(summary = "이메일 인증번호 요청", description = "이메일 인증번호 요청을 합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "이메일 인증번호 요청에 성공하였습니다.",
			content = @Content(schema = @Schema(implementation = Void.class))
		),
		@ApiResponse(
			responseCode = "409",
			description = "이미 존재하는 이메일입니다.",
			content = @Content(schema = @Schema(implementation = ClientErrorResponse.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "이메일 전송에 실패했습니다.",
			content = @Content(schema = @Schema(implementation = ClientErrorResponse.class))
		)
	})
	ResponseEntity<Void> emailCertification(EmailCertificationRequestDto dto);

	@Operation(summary = "회원가입 요청", description = "회원가입 요청을 합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "회원가입에 성공하였습니다.",
			content = @Content(schema = @Schema(implementation = User.class))
		),
		@ApiResponse(
			responseCode = "400",
			description = "인증번호가 일치하지 않습니다.",
			content = @Content(schema = @Schema(implementation = ClientErrorResponse.class))
		),
		@ApiResponse(
			responseCode = "409",
			description = "이미 존재하는 이메일입니다.",
			content = @Content(schema = @Schema(implementation = ClientErrorResponse.class))
		)
	})
	ResponseEntity<User> signUp(SignUpRequestDto dto);

	@Operation(summary = "로그인 요청", description = "로그인 요청을 합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "로그인에 성공하였습니다.",
			content = @Content(schema = @Schema(implementation = SignInResponseDto.class))
		),
		@ApiResponse(
			responseCode = "404",
			description = "존재하지 않는 사용자입니다.",
			content = @Content(schema = @Schema(implementation = ClientErrorResponse.class))
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증에 실패했습니다.",
			content = @Content(schema = @Schema(implementation = ClientErrorResponse.class))
		)
	})
	ResponseEntity<SignInResponseDto> signIn(SignInRequestDto dto);
}