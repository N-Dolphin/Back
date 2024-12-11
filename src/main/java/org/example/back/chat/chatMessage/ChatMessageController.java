package org.example.back.chat.chatMessage;



import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.back.chat.chatRoom.ChatRoom;
import org.example.back.chat.chatRoom.ChatRoomRepository;
import org.example.back.chat.chatRoom.ChatRoomService;
import org.example.back.chat.chatRoom.ChatRoomServiceImpl;
import org.example.back.chat.chatroommember.ChatRoomParticipant;
import org.example.back.chat.common.constant.MessageType;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatMessageDeleteRequest;
import org.example.back.chat.common.dto.ChatMessagesResponse;
import org.example.back.chat.common.dto.ChatRoomEnterRequest;
import org.example.back.chat.common.dto.EnterConfirmRes;
import org.example.back.chat.common.dto.FileInfo;
import org.example.back.chat.common.dto.MessageRes;
import org.example.back.chat.exception.ChatRoomAccessDeniedException;
import org.example.back.chat.exception.ChatRoomNotFoundException;
import org.example.back.chat.exception.ChatRoomNotValidException;
import org.example.back.chat.exception.dto.WebSocketErrorResponse;
import org.example.back.chat.util.StompHeaderAccessorUtil;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController implements ChatMessageControllerSwagger {

	private final ChatMessageServiceImpl chatMessageService;
	private final StompHeaderAccessorUtil stompHeaderAccessorUtil;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomRepository chatRoomRepository;
	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;
	private final AmazonS3 amazonS3;
	private final ChatRoomServiceImpl chatRoomServiceImpl;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@MessageMapping("chat.message")
	public void sendMessage(StompHeaderAccessor accessor, ChatDto.ChatMessageReq message) {
		try {

			if ("FILE".equals(message.getMessageType())) {
				message.setMessageType(MessageType.FILE_MESSAGE);
			}

			if (message.getContent().startsWith("data:")) {
				String[] parts = message.getContent().split(",");
				String contentType = parts[0].split(":")[1].split(";")[0];
				String base64Data = parts[1];
				byte[] fileData = Base64.getDecoder().decode(base64Data);

				// S3에 업로드
				String fileName = generateUniqueFileName(contentType);
				String fileUrl = uploadToS3(fileData, contentType, fileName);

				// FileInfo 생성 - null 체크 없이 새로 생성
				FileInfo fileInfo = new FileInfo(
					fileName,  // 원본 파일명 대신 생성된 파일명 사용
					fileUrl,
					contentType,
					(int)fileData.length,
					null
				);

				// 새 메시지 생성
				message = ChatDto.ChatMessageReq.builder()
					.content(fileUrl)
					.messageType(MessageType.FILE_MESSAGE)
					.fileInfo(fileInfo)
					.build();
			}

			MessageRes messageRes = chatMessageService.sendMessage(accessor, message);
			System.out.println("Message sent successfully: " + messageRes);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to send message: " + e.getMessage());
		}
	}

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		chatMessageService.handleInitialConnect(accessor);
	}

	@MessageMapping("chat.enter")
	public void  enterChatRoom(StompHeaderAccessor accessor, ChatRoomEnterRequest request) {
		try {


			log.info("Enter chat room attempt - roomId: {}", request.getChatRoomId());

			Long profileId = stompHeaderAccessorUtil.getMemberIdInSession(accessor);
			log.info("Profile ID from session: {}", profileId);

			Long chatRoomId = request.getChatRoomId();
			log.info("Attempting to enter chat room: {}", chatRoomId);

			// 채팅방 ID를 세션에 저장
			stompHeaderAccessorUtil.setChatRoomIdInSession(accessor, chatRoomId);
			log.info("Chat room ID saved in session");



			// 채팅방 ID를 세션에 저장
			stompHeaderAccessorUtil.setChatRoomIdInSession(accessor, chatRoomId);

			// 채팅방 존재 여부 및 참가 자격 확인
			if (!chatRoomServiceImpl.isAccessibleChatRoom(chatRoomId, profileId)) {
				throw new ChatRoomAccessDeniedException("접근할 수 없는 채팅방입니다.");
			}

			// 채팅방 조회 및 참가자 구분
			ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
				.orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));

			List<Long> participantIds = chatRoom.getParticipants().stream()
				.map(ChatRoomParticipant::getProfileId)
				.sorted()
				.toList();

			if (participantIds.size() != 2) {
				throw new ChatRoomNotValidException("잘못된 채팅방 구성입니다.");
			}

			// 방장(RoomMaker)과 게스트(Guest) 정보 생성
			ChatDto.ChatRoomCreateRes roomInfo = ChatDto.ChatRoomCreateRes.createRes(
				chatRoom.getId(),
				participantIds.get(0),
				participantIds.get(1)
			);

			// roomInfo로 참가자 정보 전송
			messagingTemplate.convertAndSend(
				"/exchange/chat.exchange/room." + chatRoomId,
				roomInfo
			);

			// 입장 확인 메시지는 단순하게 전송
			messagingTemplate.convertAndSend(
				"/exchange/chat.exchange/room." + chatRoomId,
				new EnterConfirmRes(profileId, chatRoomId)
			);

			System.out.println("Chat room entered - Room ID: " + chatRoomId + ", Profile ID: " + profileId);

		}
		catch (ChatRoomAccessDeniedException e) {
			// 클라이언트에게 에러 메시지 전송
			messagingTemplate.convertAndSendToUser(
				accessor.getSessionId(),
				"/queue/errors",
				new WebSocketErrorResponse(
					"CHAT_ROOM_ACCESS_DENIED",
					e.getMessage()
				)
			);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		chatMessageService.handleDisconnectMessage(accessor);
	}


	// @GetMapping("/chat-messages/chat-rooms/{chatRoomId}")
	// public ResponseEntity<List<MessageRes>> getChatMessages(
	// 	@PathVariable("chatRoomId") Long chatRoomId,
	// 	@RequestParam(name = "page", defaultValue = "0") int page,
	// 	@RequestParam(name = "size", defaultValue = "5") int size
	// )  {
	// 	List<MessageRes> chatMessageResList = chatMessageService.getChatMessages(chatRoomId, page, size);
	// 	return ResponseEntity.ok(chatMessageResList);
	// }

	@GetMapping("/api/v1/chat-messages/chat-rooms/{chatRoomId}")
	@Override
	public ResponseEntity<ChatMessagesResponse> getChatMessages(
		@PathVariable("chatRoomId") Long chatRoomId,
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "5") int size
	) {
		Page<MessageRes> chatMessagePage = chatMessageService.getChatMessages(chatRoomId, page, size);

		ChatMessagesResponse response = new ChatMessagesResponse(
			chatMessagePage.getContent(),
			chatMessagePage.getNumber(),
			chatMessagePage.getTotalPages(),
			chatMessagePage.hasNext(),
			chatMessagePage.getTotalElements()
		);

		return ResponseEntity.ok(response);
	}


	private String uploadToS3(byte[] fileData, String contentType, String fileName) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(contentType);
		metadata.setContentLength(fileData.length);

		try {
			amazonS3.putObject(new PutObjectRequest(
				bucketName,
				fileName,
				new ByteArrayInputStream(fileData),
				metadata
			));
			return amazonS3.getUrl(bucketName, fileName).toString();
		} catch (Exception e) {
			throw new RuntimeException("Failed to upload file to S3", e);
		}
	}

	private String generateUniqueFileName(String contentType) {
		return System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + getFileExtension(contentType);
	}

	private String getFileExtension(String contentType) {
		switch (contentType) {
			case "image/jpeg": return ".jpg";
			case "image/png": return ".png";
			case "image/gif": return ".gif";
			default: return "";
		}
	}



	@MessageMapping("chat.delete")
	public void deleteMessage(StompHeaderAccessor accessor, ChatMessageDeleteRequest request) {
		Long profileId = stompHeaderAccessorUtil.getMemberIdInSession(accessor);
		chatMessageService.deleteMessage(request.getChatRoomId(), profileId, request.getMessageId());
	}


	@DeleteMapping("/api/v1/chat-rooms/{chatRoomId}/leave")
	public ResponseEntity<Void> leaveChatRoom(
		@PathVariable("chatRoomId") Long chatRoomId,
		HttpServletRequest request
	) {
		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);

		chatRoomServiceImpl.leaveChatRoom(chatRoomId, profileId);
		return ResponseEntity.ok().build();
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}




