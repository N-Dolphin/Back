package org.example.back.profileimage;

import org.example.back.location.LocationRequest;
import org.example.back.profile.controller.request.ProfileCreateRequest;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Profile API", description = "프로필 관련 API")
public interface ProfileImageControllerSwagger {


	@Operation(
		summary = "프로필 이미지 업로드",
		description = "사용자의 프로필 이미지를 업로드하고 업로드된 파일의 경로 URL을 반환합니다.",
		parameters = {
			@Parameter(name = "profileImage", description = "업로드할 프로필 이미지 파일", required = true, content = @Content(
				mediaType = "multipart/form-data",
				schema = @Schema(type = "string", format = "binary", description = "파일 형식"),
				examples = @ExampleObject(value = "Select files")
			))
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "프로필 이미지 업로드 성공",
				content = @Content(schema = @Schema(type = "string"))
			),
			@ApiResponse(
				responseCode = "400",
				description = "파일이 없거나 잘못된 요청",
				content = @Content
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 오류로 인한 파일 업로드 실패",
				content = @Content
			)
		}
	)
	ResponseEntity<String> uploadProfileImage(@RequestParam("profileImage") MultipartFile file);
}
