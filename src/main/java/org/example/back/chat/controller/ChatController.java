package org.example.back.chat.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.example.back.chat.dto.ChatRoomDto;
import org.example.back.chat.dto.MessageResponseDto;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.repository.ChatRoomRepository;
import org.example.back.chat.service.ChatService;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.rabbitmq.MessageDto;
import org.example.back.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController implements ChatControllerSwagger {
	private final ChatService chatService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final ChatRoomRepository chatRoomRepository;

	@Override
	@GetMapping("/rooms")
	public ResponseEntity<List<ChatRoomDto>> getChatRooms(HttpServletRequest request) {
		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);
		List<ChatRoomDto> chatRooms = chatService.getChatRooms(profileId);
		return ResponseEntity.ok(chatRooms);
	}

	@Override
	@GetMapping("/rooms/{chatRoomId}/getMessages")
	public ResponseEntity<MessageResponseDto> getMessages(
		@PathVariable("chatRoomId") Long chatRoomId,
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "20") int size,
		HttpServletRequest request
	) {
		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);

		if (!chatService.isUserInChatRoom(profileId, chatRoomId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		List<MessageDto> messages = chatService.getMessages(chatRoomId, page, size);
		boolean hasMore = chatService.hasMoreMessages(chatRoomId, page, size);

		return ResponseEntity.ok(new MessageResponseDto(messages, hasMore));
	}


	@PostMapping("/rooms/{chatRoomId}/sendMessages")
	public ResponseEntity<String> sendMessage(
		@PathVariable("chatRoomId") Long chatRoomId,
		@RequestBody MessageDto messageDto,
		HttpServletRequest request
	) {
		String token = resolveToken(request);
		String userIdStr = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdStr);
		Long senderProfileId = userService.getProfileIdByUserId(userId);

		List<Object[]> profileIds = chatRoomRepository.findProfileIdsByChatRoomId(chatRoomId);

		if (!profileIds.isEmpty()) {
			Long fromProfileId = (Long) profileIds.get(0)[0];
			Long toProfileId = (Long) profileIds.get(0)[1];

			MessageDto newMessageDto = new MessageDto(
				null,                           // messageId (DB 저장 전)
				senderProfileId,                // fromProfileId
				senderProfileId.equals(fromProfileId) ? toProfileId : fromProfileId,  // toProfileId
				messageDto.content(),           // content
				LocalDateTime.now(),            // sendAt
				chatRoomId,                     // chatRoomId
				ChatMessage.MessageStatus.SENT,             // status
				null,                           // deliveredAt
				null                            // readAt
			);

			chatService.sendMessage(
				newMessageDto.fromProfileId(),
				newMessageDto.toProfileId(),
				newMessageDto.content(),
				chatRoomId
			);

			return ResponseEntity.ok("Message sent successfully");
		}

		return ResponseEntity.badRequest().body("Chat room participants not found");
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
