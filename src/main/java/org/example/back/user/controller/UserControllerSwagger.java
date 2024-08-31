package org.example.back.user.controller;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface UserControllerSwagger {

	@Operation(summary = "이메일 인증번호 요청", description = "이메일 인증번호 요청을 합니다.")
	@ApiResponse(responseCode = "200", description = "이메일 인증번호 요청에 성공하였습니다.")
	ResponseEntity<EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto);

	@Operation(summary = "인증번호 전송", description = "이메일 인증번호 전송을 합니다.")
	@ApiResponse(responseCode = "200", description = "인증번호가 일치합니다.")
	ResponseEntity<CheckCertificationResponseDto> checkCertificationNumber(CheckCertificationRequestDto dto);

	@Operation(summary = "회원가입 요청", description = "회원가입 요청을 합니다.")
	@ApiResponse(responseCode = "200", description = "회원가입 요청에 성공하였습니다.")
	ResponseEntity<User> signUp(SignUpRequestDto dto);

	@Operation(summary = "로그인 요청", description = "로그인 요청을 합니다.")
	@ApiResponse(responseCode = "200", description = "로그인에 성공하였습니다.")
	ResponseEntity<SignInResponseDto> signIn(SignInRequestDto dto);

}
