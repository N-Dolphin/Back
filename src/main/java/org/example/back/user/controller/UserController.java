package org.example.back.user.controller;

import org.example.back.user.dto.User;
import org.example.back.user.dto.request.CheckCertificationRequestDto;
import org.example.back.user.dto.request.EmailCertificationRequestDto;
import org.example.back.user.dto.request.SignInRequestDto;
import org.example.back.user.dto.request.SignUpRequestDto;
import org.example.back.user.dto.response.CheckCertificationResponseDto;
import org.example.back.user.dto.response.EmailCertificationResponseDto;
import org.example.back.user.dto.response.SignInResponseDto;
import org.example.back.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
@Tag(name = "User", description = "User 관련 API")

public class UserController implements UserControllerSwagger {

	private final UserService userService;

	@PostMapping("/email-certification")
	@Override
	public ResponseEntity<EmailCertificationResponseDto> emailCertification(
		@RequestBody @Valid  EmailCertificationRequestDto dto
	) {
		EmailCertificationResponseDto responseDto = userService.emailCertification(dto);
		return ResponseEntity.ok(responseDto);
	}

	@PostMapping("/check-certification")
	@Override
	public ResponseEntity<CheckCertificationResponseDto> checkCertificationNumber(
		@RequestBody @Valid  CheckCertificationRequestDto dto) {
		CheckCertificationResponseDto responseDto = userService.checkCertificationNumber(dto);
		return ResponseEntity.ok(responseDto);
	}

	@PostMapping("/sign-up")
	@Override
	public ResponseEntity<User> signUp(
		@RequestBody @Valid  SignUpRequestDto dto) {
		User user = userService.signUp(dto);
		return ResponseEntity.ok(user);
	}

	@PostMapping("/sign-in")
	@Override
	public ResponseEntity<SignInResponseDto> signIn(
		@RequestBody @Valid  SignInRequestDto dto) {
		SignInResponseDto responseDto = userService.signIn(dto);
		return ResponseEntity.ok(responseDto);
	}

}
