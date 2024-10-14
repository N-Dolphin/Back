package org.example.back.profile.controller;

import org.example.back.location.LocationRequest;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.ProfileDto;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.example.back.profile.domain.Profile;
import org.springframework.http.ResponseEntity;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Profile API", description = "프로필 관련 API")
public interface ProfileControllerSwagger {

	@Operation(
		summary = "프로필 생성",
		description = "주어진 요청을 기반으로 새로운 프로필을 생성합니다.",
		requestBody = @RequestBody(content =
		@Content(schema = @Schema(implementation = ProfileCreateRequest.class))),
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "프로필 생성 성공",
				content = @Content(schema = @Schema(implementation = ProfileCreateResponse.class))
			),
			@ApiResponse(
				responseCode = "409",
				description = "이미 프로필이 존재하는 경우",
				content = @Content
			)
		}
	)
	ResponseEntity<ProfileCreateResponse> createProfile(final ProfileCreateRequest request, HttpServletRequest httpServletRequest);

	@Operation(
		summary = "위치 저장",
		description = "프로필의 위치 정보를 저장합니다.",
		requestBody = @RequestBody(content =
		@Content(schema = @Schema(implementation = LocationRequest.class))),
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "위치 저장 성공",
				content = @Content(schema = @Schema(implementation = Profile.class))
			),
			@ApiResponse(
				responseCode = "404",
				description = "프로필을 찾을 수 없는 경우",
				content = @Content
			)
		}
	)
	ResponseEntity<ProfileDto> saveLocation(@RequestBody LocationRequest locationRequest, HttpServletRequest httpServletRequest);

	@Operation(
		summary = "청와대와의 거리 계산",
		description = "청와대와의 거리 계산 결과를 반환합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "거리 계산 성공",
				content = @Content(schema = @Schema(implementation = Profile.class))
			)
		}
	)
	ResponseEntity<List<Profile>> getDistanceFrom10(HttpServletRequest httpServletRequest);


	@Operation(
		summary = "유저와의 프로필 계산",
		description = "계산 결과(프로필 리스트)를 반환합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "리스트 반환 성공",
				content = @Content(schema = @Schema(implementation = Profile.class))
			)
		}
	)
	ResponseEntity<List<ProfileDto>> findProfiles(HttpServletRequest httpServletRequest);
}
