package org.example.back.profileimage;

import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.profileimage.exception.FileEmptyException;
import org.example.back.profileimage.exception.FileSizeExceededException;
import org.example.back.profileimage.exception.FileUploadException;
import org.example.back.profileimage.exception.InvalidFileTypeException;
import org.example.back.profileimage.exception.S3UploadException;
import org.example.back.profileimage.service.ProfileImageService;
import org.example.back.user.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
	public ResponseEntity<String> uploadProfileImage(@RequestParam("profileImage") List<MultipartFile> file, HttpServletRequest request) {
		if (file.isEmpty()) {
			throw new FileEmptyException("업로드할 파일이 없습니다.");
		}

		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);

		List<String> imgUrlList = new ArrayList<>();
		for (MultipartFile multipartFile : file) {
			try {
				validateImageFile(multipartFile);  // 이미지 파일 검증
				String fileUrl = uploadFileToS3(multipartFile);
				profileImageService.saveProfileImage(profileId, fileUrl, (int)multipartFile.getSize());
				imgUrlList.add(fileUrl);
			} catch (IOException e) {
				throw new FileUploadException("파일 업로드 중 오류가 발생했습니다: " + multipartFile.getOriginalFilename(), e);
			}
		}

		return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다: " + imgUrlList.get(0));
	}

	private void validateImageFile(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new InvalidFileTypeException("이미지 파일만 업로드 가능합니다.");
		}

		// 파일 크기 제한 체크 (예: 5MB)
		if (file.getSize() > 10 * 1024 * 1024) {
			throw new FileSizeExceededException("파일 크기는 10MB를 초과할 수 없습니다.");
		}
	}
	private String uploadFileToS3(MultipartFile file) throws IOException {
		String fileName = file.getOriginalFilename();

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		try (InputStream inputStream = file.getInputStream()) {
			PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
			amazonS3.putObject(putRequest);
			return amazonS3.getUrl(bucketName, fileName).toString();
		} catch (AmazonS3Exception e) {
			throw new S3UploadException("S3 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
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
