package org.example.back.chat.chatRoom;

import org.example.back.chat.common.dto.ChatDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
class ChatRoomController {

	private final ChatRoomService chatRoomService;

	// @PostMapping("/chat-rooms")
	// public ResponseEntity<ChatDto.ChatRoomCreateRes> createChatRoom(@RequestParam Long loginId,
	// 	@RequestBody ChatDto.ChatRoomCreateReq request) {
	// 	return ResponseEntity.ok(chatRoomService.createChatRoomForPersonal(loginId, request));
	// }

	@GetMapping("/chat-rooms")
	public ResponseEntity getChatRooms(@RequestParam Long loginId) {
		return ResponseEntity.ok(chatRoomService.getChatRooms(loginId));
	}
}