package org.example.back.profileimage;

import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.profileimage.service.ProfileImageService;
import org.example.back.user.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/profiles/upload-profile")
@RequiredArgsConstructor
public class ProfileImageController implements ProfileImageControllerSwagger {

	private final AmazonS3 amazonS3;
	private final ProfileImageService profileImageService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName; // S3 버킷 이름

	@PostMapping()
	@Override
	public ResponseEntity<String> uploadProfileImage(@RequestParam("profileImage") MultipartFile file, HttpServletRequest request) {
		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body("파일이 없습니다.");
		}

		try {
			// 파일 이름 추출
			String fileName = file.getOriginalFilename();

			// 이미지 사이즈 얻기
			Integer imageSize = (int) file.getSize(); // 파일 크기를 바이트 단위로 얻기

			// S3에 업로드
			InputStream inputStream = file.getInputStream();
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			metadata.setContentType(file.getContentType()); // 요청받은 파일의 ContentType 메타데이터에 주입

			PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
			amazonS3.putObject(putRequest);

			String token = resolveToken(request);
			String userIdToken = jwtTokenProvider.extractSubject(token);
			Long userId = Long.valueOf(userIdToken);

			Long profileId = userService.getProfileIdByUserId(userId);

			System.out.println(token);
			System.out.println(userIdToken);
			System.out.println(userId);
			System.out.println(profileId);

			// 파일 경로를 URL로 변환 (프론트엔드에 보낼 URL 생성)
			String fileDownloadUri = amazonS3.getUrl(bucketName, fileName).toString();

			//파일 경로를 DB에 저장
			profileImageService.saveProfileImage(profileId,fileDownloadUri, imageSize);



			return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다: " + fileDownloadUri);
		} catch (IOException e) {
			return ResponseEntity.status(500).body("파일 업로드 중 오류가 발생했습니다.");
		}
	}
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
