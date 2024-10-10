package org.example.back.profileimage;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Image API", description = "프로필 이미지 관련 API")
public class ProfileImageController implements ProfileImageControllerSwagger {

	private final AmazonS3 amazonS3;

	@Value("${cloud.aws.s3.bucket-name}")
	private String bucketName; // S3 버킷 이름

	@PostMapping("/upload-profile")
	@Override
	public ResponseEntity<String> uploadProfileImage(@RequestParam("profileImage") MultipartFile file) {
		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body("파일이 없습니다.");
		}

		try {
			// 파일 이름 추출
			String fileName = file.getOriginalFilename();
			// S3에 업로드
			InputStream inputStream = file.getInputStream();
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
			amazonS3.putObject(putRequest);

			// 파일 경로를 URL로 변환 (프론트엔드에 보낼 URL 생성)
			String fileDownloadUri = amazonS3.getUrl(bucketName, fileName).toString();

			return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다: " + fileDownloadUri);
		} catch (IOException e) {
			return ResponseEntity.status(500).body("파일 업로드 중 오류가 발생했습니다.");
		}
	}
}
