package org.example.back.chat.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final ChatRoomRepository chatRoomRepository;

	// 사용자가 속한 모든 채팅방 목록을 조회
	@GetMapping("/rooms")
	public ResponseEntity<List<ChatRoom>> getChatRooms(HttpServletRequest request) {

		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);
		List<ChatRoom> chatRooms = chatService.getChatRooms(profileId);
		return ResponseEntity.ok(chatRooms);
	}

	// 채팅방 내 메시지 조회
	@GetMapping("/rooms/{chatRoomId}/messages")
	public ResponseEntity<List<ChatMessage>> getMessages(
		@PathVariable Long chatRoomId,
		HttpServletRequest request
	) {
		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);

		if (!chatService.isUserInChatRoom(profileId, chatRoomId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		List<ChatMessage> messages = chatService.getMessages(chatRoomId);
		return ResponseEntity.ok(messages);
	}

	// 메시지 전송
	// 전달하는 파라미터는... 채팅방을 누르면 거기 MatchingEvent를 찾아서, 전송하는 profileID와 수신 ID를 구분,
	@PostMapping("/rooms/{chatRoomId}/messages")
	public ResponseEntity<String> sendMessage(
		@PathVariable("chatRoomId") Long chatRoomId,
		@RequestBody MessageDto messageDto,
		HttpServletRequest request
	) {

		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);

		if (!chatService.isUserInChatRoom(profileId, chatRoomId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		List<Object[]> profileIds = chatRoomRepository.findProfileIdsByChatRoomId(chatRoomId);

		if (!profileIds.isEmpty()) {
			Long fromProfileId = (Long) profileIds.get(0)[0];
			Long toProfileId = (Long) profileIds.get(0)[1];

			System.out.println(fromProfileId);

			chatService.sendMessage(fromProfileId,toProfileId, messageDto.content());
			System.out.println(messageDto.content());
			return ResponseEntity.ok("메시지 전송 성공");
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
