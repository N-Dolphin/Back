package org.example.back.chat.chatMessage;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.example.back.chat.chatRoom.ChatRoom;
import org.example.back.chat.chatRoom.ChatRoomRepository;
import org.example.back.chat.chatroommember.ChatRoomParticipant;
import org.example.back.chat.common.constant.MessageType;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatRoomEnterRequest;
import org.example.back.chat.common.dto.EnterConfirmRes;
import org.example.back.chat.common.dto.FileInfo;
import org.example.back.chat.common.dto.MessageRes;
import org.example.back.chat.util.StompHeaderAccessorUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class ChatMessageController {

	private final ChatMessageServiceImpl chatMessageService;
	private final StompHeaderAccessorUtil stompHeaderAccessorUtil;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomRepository chatRoomRepository;
	private final AmazonS3 amazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;


	// Destination Queue: /pub/chat.message를 통해 호출 후 처리 되는 로직

	// @MessageMapping("chat.message")
	// public void sendMessage(StompHeaderAccessor accessor, ChatDto.ChatMessageReq message) {
	// 	try {
	// 		// base64 이미지인 경우 처리
	// 		if (message.getContent().startsWith("data:")) {
	// 			String[] parts = message.getContent().split(",");
	// 			String contentType = parts[0].split(":")[1].split(";")[0];
	// 			String base64Data = parts[1];
	// 			byte[] fileData = Base64.getDecoder().decode(base64Data);
	//
	// 			// S3에 업로드
	// 			String fileName = generateUniqueFileName(contentType);
	// 			String fileUrl = uploadToS3(fileData, contentType, fileName);
	//
	// 			// URL을 content로 설정
	// 			message = ChatDto.ChatMessageReq.builder()
	// 				.content(fileUrl)
	// 				.build();
	// 		}
	//
	// 		// 기존 메시지 처리 로직
	// 		MessageRes messageRes = chatMessageService.sendMessage(accessor, message);
	// 		System.out.println("Message sent successfully: " + messageRes);
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		System.err.println("Failed to send message: " + e.getMessage());
	// 	}
	// }

	// @MessageMapping("chat.message")
	// public void sendMessage(StompHeaderAccessor accessor, ChatDto.ChatMessageReq message) {
	// 	try {
	// 		if (message.getContent().startsWith("data:")) {
	// 			String[] parts = message.getContent().split(",");
	// 			String contentType = parts[0].split(":")[1].split(";")[0];
	// 			String base64Data = parts[1];
	// 			byte[] fileData = Base64.getDecoder().decode(base64Data);
	//
	// 			// S3에 업로드
	// 			String fileName = generateUniqueFileName(contentType);
	// 			String fileUrl = uploadToS3(fileData, contentType, fileName);
	//
	// 			// FileInfo 생성
	// 			FileInfo fileInfo = new FileInfo(
	// 				message.getFileInfo().getFileName(),  // 원본 파일명 유지
	// 				fileUrl,
	// 				contentType,
	// 				message.getFileInfo().getFileSize(),
	// 				null  // 썸네일은 필요한 경우 추가
	// 			);
	//
	// 			// 새 메시지 생성
	// 			message = ChatDto.ChatMessageReq.builder()
	// 				.content(fileUrl)
	// 				.messageType(MessageType.FILE_MESSAGE)  // FILE_MESSAGE로 설정
	// 				.fileInfo(fileInfo)
	// 				.build();
	// 		}
	//
	// 		MessageRes messageRes = chatMessageService.sendMessage(accessor, message);
	// 		System.out.println("Message sent successfully: " + messageRes);
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		System.err.println("Failed to send message: " + e.getMessage());
	// 	}
	// }

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
			Long profileId = stompHeaderAccessorUtil.getMemberIdInSession(accessor); // 접속한 사용자의 Profile ID
			Long chatRoomId = request.getChatRoomId();

			// 채팅방 ID를 세션에 저장
			stompHeaderAccessorUtil.setChatRoomIdInSession(accessor, chatRoomId);

			// 채팅방 조회 및 참가자 구분
			ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
				.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

			List<Long> participantIds = chatRoom.getParticipants().stream()
				.map(ChatRoomParticipant::getProfileId)
				.sorted()
				.toList();

			if (participantIds.size() != 2) {
				throw new RuntimeException("잘못된 채팅방 구성입니다.");
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

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		chatMessageService.handleDisconnectMessage(accessor);
	}

	@GetMapping("/chat-messages/chat-rooms/{chatRoomId}")
	public ResponseEntity getChatMessages(@PathVariable("chatRoomId") Long chatRoomId) {
		List<MessageRes> chatMessageResList = chatMessageService.getChatMessages(chatRoomId);
		return ResponseEntity.ok(chatMessageResList);
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
}




