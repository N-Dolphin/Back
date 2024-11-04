package org.example.back.chat.controller;

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
	@GetMapping("/rooms/{chatRoomId}/messages")
	public ResponseEntity<MessageResponseDto> getMessages(
		@PathVariable Long chatRoomId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
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


	@Override
	@PostMapping("/rooms/{chatRoomId}/messages")
	public ResponseEntity<String> sendMessage(
		@PathVariable("chatRoomId") Long chatRoomId,
		@RequestBody MessageDto messageDto,
		HttpServletRequest request
	) {
		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long senderProfileId = userService.getProfileIdByUserId(userId);

		if (!chatService.isUserInChatRoom(senderProfileId, chatRoomId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		List<Object[]> profileIds = chatRoomRepository.findProfileIdsByChatRoomId(chatRoomId);

		if (!profileIds.isEmpty()) {
			Long fromProfileId = (Long) profileIds.get(0)[0];
			Long toProfileId = (Long) profileIds.get(0)[1];

			// 현재 사용자의 profileId가 fromProfileId와 일치하면 그대로 사용
			// 일치하지 않으면 현재 사용자가 수신자이므로 방향을 바꿔서 전송
			if (senderProfileId.equals(fromProfileId)) {
				chatService.sendMessage(fromProfileId, toProfileId, messageDto.content(),chatRoomId);
				System.out.println("그대로 전송");
			} else if (senderProfileId.equals(toProfileId)) {
				chatService.sendMessage(toProfileId, fromProfileId, messageDto.content(),chatRoomId);
				System.out.println("반대로 전송");
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}			return ResponseEntity.ok("메시지 전송 성공");
		}

		return ResponseEntity.ok("profileId 확인 불가");
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
