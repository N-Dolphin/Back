package org.example.back.profileimage;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Image API", description = "프로필 이미지 관련 API")
public class ProfileImageController implements ProfileImageControllerSwagger {

	// 파일이 저장될 경로를 설정 (properties 파일에서 값을 가져오거나 고정값을 설정할 수 있음)
	// @Value("${profile.image.upload-dir}")
	// private String uploadDir;

	@PostMapping("/upload-profile")
	@Override
	public ResponseEntity<String> uploadProfilePicture(@RequestParam("profileImage") MultipartFile file) {
		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body("파일이 없습니다.");
		}

		try {
			// 파일 이름 추출
			String fileName = file.getOriginalFilename();

			// 파일 저장 경로 생성
			//String filePath = uploadDir + File.separator + fileName;

			// 파일을 저장
			//File dest = new File(filePath);
			//file.transferTo(dest);

			// 파일 경로를 URL로 변환 (프론트엔드에 보낼 URL 생성)
			// String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
			// 	.path("/uploads/")
			// 	.path(fileName)
			// 	.toUriString();

			//return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다: " + fileDownloadUri);
			return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다: ");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("파일 업로드 중 오류가 발생했습니다.");
		}
	}

}
