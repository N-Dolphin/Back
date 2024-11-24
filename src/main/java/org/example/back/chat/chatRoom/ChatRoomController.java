package org.example.back.chat.chatRoom;

import java.util.List;

import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatRoomParticipantsRecord;
import org.example.back.chat.common.dto.SimpleChatRoomRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	private final ChatRoomServiceImpl chatRoomService;

	// @PostMapping("/chat-rooms")
	// public ResponseEntity<ChatDto.ChatRoomCreateRes> createChatRoom(@RequestParam Long loginId,
	// 	@RequestBody ChatDto.ChatRoomCreateReq request) {
	// 	return ResponseEntity.ok(chatRoomService.createChatRoomForPersonal(loginId, request));
	// }

	@GetMapping("/chat-rooms")
	public ResponseEntity getChatRooms(@RequestParam Long loginId) {
		return ResponseEntity.ok(chatRoomService.getChatRooms(loginId));
	}

	@GetMapping("/chat-rooms/simple")
	public ResponseEntity<List<SimpleChatRoomRecord>> getSimpleChatRooms(@RequestParam Long loginId) {
		return ResponseEntity.ok(chatRoomService.getSimpleChatRooms(loginId));
	}

	@GetMapping("/chat-rooms/{roomId}/participants")
	public ResponseEntity<ChatRoomParticipantsRecord> getChatRoomParticipants(@PathVariable Long roomId) {
		return ResponseEntity.ok(chatRoomService.getChatRoomParticipants(roomId));
	}


}