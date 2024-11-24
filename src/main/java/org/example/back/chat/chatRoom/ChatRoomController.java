package org.example.back.chat.chatRoom;

import java.util.List;

import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatRoomParticipantsRecord;
import org.example.back.chat.common.dto.SimpleChatRoomRecord;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.service.UserService;
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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1")
class ChatRoomController implements  ChatRoomControllerSwagger{

	private final ChatRoomServiceImpl chatRoomService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;

	@GetMapping("/chat-rooms/simple")
	@Override
	public ResponseEntity<List<SimpleChatRoomRecord>> getSimpleChatRooms(HttpServletRequest request) {
		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);

		return ResponseEntity.ok(chatRoomService.getSimpleChatRooms(profileId));
	}

	@GetMapping("/chat-rooms/{roomId}/participants")
	@Override
	public ResponseEntity<ChatRoomParticipantsRecord> getChatRoomParticipants(@PathVariable("roomId") Long roomId) {
		return ResponseEntity.ok(chatRoomService.getChatRoomParticipants(roomId));
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}