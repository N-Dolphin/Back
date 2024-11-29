package org.example.back.user.controller;

import java.util.Optional;

import org.example.back.profile.domain.Profile;
import org.example.back.user.dto.User;
import org.example.back.user.dto.request.CheckCertificationRequestDto;
import org.example.back.user.dto.request.EmailCertificationRequestDto;
import org.example.back.user.dto.request.SignInRequestDto;
import org.example.back.user.dto.request.SignUpRequestDto;
import org.example.back.user.dto.response.CheckCertificationResponseDto;
import org.example.back.user.dto.response.EmailCertificationResponseDto;
import org.example.back.user.dto.response.SignInResponseDto;
import org.example.back.user.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

	@Override
	@PostMapping("/email-certification")
	public ResponseEntity<Void> emailCertification(
		@RequestBody @Valid final EmailCertificationRequestDto dto
	) {
		userService.emailCertification(dto);
		return ResponseEntity.ok().build();
	}

	@Override
	@PostMapping("/sign-up")
	public ResponseEntity<User> signUp(
		@RequestBody @Valid final SignUpRequestDto dto
	) {
		User user = userService.signUp(dto);
		return ResponseEntity.ok(user);
	}

	@Override
	@PostMapping("/sign-in")
	public ResponseEntity<SignInResponseDto> signIn(
		@RequestBody @Valid final SignInRequestDto dto
	) {
		SignInResponseDto responseDto = userService.signIn(dto);
		return ResponseEntity.ok(responseDto);
	}
}