package org.example.back.chat.chatMessage;

import java.io.IOException;
import java.io.InputStream;

import org.example.back.chat.common.constant.MessageType;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatMessageRes;
import org.example.back.chat.common.dto.FileInfo;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chat/upload")
@RequiredArgsConstructor
public class ChatFileController {
	private final AmazonS3 amazonS3;
	private final ChatMessageServiceImpl chatMessageService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final SimpMessagingTemplate messagingTemplate;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@PostMapping
	public ResponseEntity<String> uploadChatFile(
		@RequestParam("file") MultipartFile file,
		@RequestParam("chatRoomId") Long chatRoomId,
		HttpServletRequest request
	) {
		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body("파일이 없습니다.");
		}

		try {
			// 토큰에서 사용자 정보 추출
			String token = resolveToken(request);
			String userIdToken = jwtTokenProvider.extractSubject(token);
			Long userId = Long.valueOf(userIdToken);
			Long profileId = userService.getProfileIdByUserId(userId);

			// 파일 정보 준비
			String fileName = generateUniqueFileName(file.getOriginalFilename());

			// S3에 업로드
			String fileUrl = uploadToS3(file, fileName);

			// 채팅 메시지 생성 및 전송 (기존 구조 유지)
			ChatDto.ChatMessageReq messageReq = ChatDto.ChatMessageReq.builder()
				.content(fileUrl)  // 파일 URL을 content로 전달
				.build();

			// 메시지 생성
			ChatMessage chatMessage = messageReq.createChatMessage(chatRoomId, profileId);

			//메세지 전송
			messagingTemplate.convertAndSend(
				"/exchange/chat.exchange/room." + chatRoomId,
				ChatMessageRes.createRes(chatMessage, 0)
			);

			return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다: " + fileUrl);
		} catch (IOException e) {
			return ResponseEntity.status(500).body("파일 업로드 중 오류가 발생했습니다.");
		}
	}

	private String uploadToS3(MultipartFile file, String fileName) throws IOException {
		InputStream inputStream = file.getInputStream();
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
		amazonS3.putObject(putRequest);

		return amazonS3.getUrl(bucketName, fileName).toString();
	}

	private String generateUniqueFileName(String originalFilename) {
		return System.currentTimeMillis() + "_" + originalFilename.replaceAll("\\s+", "_");
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}