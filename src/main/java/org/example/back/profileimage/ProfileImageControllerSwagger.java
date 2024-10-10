package org.example.back.profileimage;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Profile Image API", description = "프로필 이미지 관련 API")
public interface ProfileImageControllerSwagger {

	@Operation(
		summary = "프로필 이미지 업로드",
		description = "사용자의 프로필 이미지를 업로드하고 업로드된 파일의 경로 URL을 반환합니다.",
		requestBody = @RequestBody(content =
		@Content(schema = @Schema(implementation = MultipartFile.class))),
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "프로필 이미지 업로드 성공",
				content = @Content(schema = @Schema(implementation = String.class))
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
	ResponseEntity<String> uploadProfilePicture(@RequestParam("profileImage") MultipartFile file);
}
