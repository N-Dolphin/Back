package org.example.back.profileimage;

import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.profile.exception.UnauthorizedException;
import org.example.back.profileimage.entity.ProfileImage;
import org.example.back.profileimage.exception.FileEmptyException;
import org.example.back.profileimage.exception.FileSizeExceededException;
import org.example.back.profileimage.exception.FileUploadException;
import org.example.back.profileimage.exception.InvalidFileTypeException;
import org.example.back.profileimage.exception.S3UploadException;
import org.example.back.profileimage.repository.ProfileImageRepository;
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
import lombok.extern.slf4j.Slf4j;

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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles/upload-profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileImageController implements ProfileImageControllerSwagger {

	private final AmazonS3 amazonS3;
	private final ProfileImageService profileImageService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final ProfileImageRepository profileImageRepository;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@PostMapping()
	@Override
	public ResponseEntity<String> uploadProfileImage(
		@RequestParam("profileImage") List<MultipartFile> files,
		HttpServletRequest request) {

		if (files == null || files.isEmpty()) {
			throw new FileEmptyException("업로드할 파일이 없습니다.");
		}

		// 전체 파일 크기 계산
		long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
		if (totalSize > 10 * 1024 * 1024) { // 10MB
			throw new FileSizeExceededException("전체 파일 크기가 10MB를 초과할 수 없습니다.");
		}

		String token = resolveToken(request);
		if (token == null) {
			throw new UnauthorizedException("인증 토큰이 필요합니다.");
		}

		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);

		List<ProfileImage> profileImages = profileImageRepository.findByProfile_ProfileId(profileId);
		if (!profileImages.isEmpty()) {
			for (ProfileImage image : profileImages) {
				// S3에서 삭제
				deleteFileFromS3(image.getImageUrl());
			}
			// DB에서 삭제
			profileImageRepository.deleteAll(profileImages);
		}

		List<String> imgUrlList = new ArrayList<>();

		for (MultipartFile file : files) {
			try {
				validateImageFile(file);
				String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
				String fileUrl = uploadFileToS3(file, uniqueFileName);
				profileImageService.saveProfileImage(profileId, fileUrl, (int)file.getSize());
				imgUrlList.add(fileUrl);
			} catch (IOException e) {
				// 이미 업로드된 파일들 삭제
				imgUrlList.forEach(url -> deleteFileFromS3(url));
				throw new FileUploadException("파일 업로드 중 오류가 발생했습니다: " + file.getOriginalFilename(), e);
			}
		}

		return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다: " + String.join(", ", imgUrlList));
	}

	private void validateImageFile(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new InvalidFileTypeException("이미지 파일만 업로드 가능합니다.");
		}

		// 개별 파일 크기 제한 체크
		if (file.getSize() > 5 * 1024 * 1024) { // 5MB
			throw new FileSizeExceededException("개별 파일 크기는 5MB를 초과할 수 없습니다.");
		}
	}

	private String generateUniqueFileName(String originalFilename) {
		String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		return UUID.randomUUID().toString() + extension;
	}

	private String uploadFileToS3(MultipartFile file, String fileName) throws IOException {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		try (InputStream inputStream = file.getInputStream()) {
			PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata)
				.withCannedAcl(CannedAccessControlList.PublicRead); // 공개 읽기 권한 설정
			amazonS3.putObject(putRequest);
			return amazonS3.getUrl(bucketName, fileName).toString();
		} catch (AmazonS3Exception e) {
			throw new S3UploadException("S3 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	private void deleteFileFromS3(String fileUrl) {
		try {
			String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
			amazonS3.deleteObject(bucketName, fileName);
		} catch (AmazonS3Exception e) {
			log.error("S3 파일 삭제 중 오류 발생: {}", e.getMessage());
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