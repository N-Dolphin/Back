package org.example.back.chat.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.example.back.chat.dto.ChatRoomDto;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.exception.ChatException;
import org.example.back.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
	private final ChatRoomRepository chatRoomRepository;
	private final Map<Long, LocalDateTime> roomActivityCache = new ConcurrentHashMap<>();
	private static final Duration ROOM_INACTIVE_THRESHOLD = Duration.ofHours(24);

	public List<ChatRoomDto> getChatRooms(Long profileId) {
		return chatRoomRepository.findAllByFromProfileIdOrToProfileId(profileId, profileId)
			.stream()
			.map(room -> new ChatRoomDto(room.getId(), room.getFromProfileId(),
				room.getToProfileId(), room.getCreatedAt()))
			.toList();
	}

	public boolean isUserInChatRoom(Long profileId, Long chatRoomId) {
		Optional<ChatRoom> chatRoom = chatRoomRepository.findById(chatRoomId);
		return chatRoom.isPresent() &&
			(chatRoom.get().getFromProfileId().equals(profileId) ||
				chatRoom.get().getToProfileId().equals(profileId));
	}

	@Transactional
	public Long createChatRoom(Long fromProfileId, Long toProfileId) {
		Optional<ChatRoom> existingRoom = chatRoomRepository.findByProfiles(fromProfileId, toProfileId);

		if (existingRoom.isPresent()) {
			log.info("이미 존재하는 채팅방입니다: {}", existingRoom.get().getId());
			return existingRoom.get().getId();
		}

		ChatRoom chatRoom = new ChatRoom();
		chatRoom.setFromProfileId(fromProfileId);
		chatRoom.setToProfileId(toProfileId);
		chatRoomRepository.save(chatRoom);
		log.info("채팅방 생성 완료: {}", chatRoom.getId());

		return chatRoom.getId();
	}

	@Transactional(readOnly = true)
	public ChatRoom getChatRoom(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> {
				log.error("Chat room not found: {}", chatRoomId);
				return new ChatException("CHATROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.");
			});
	}

	@Transactional
	public void updateRoomActivity(Long chatRoomId) {
		LocalDateTime now = LocalDateTime.now();
		roomActivityCache.put(chatRoomId, now);

		ChatRoom chatRoom = getChatRoom(chatRoomId);
		chatRoom.updateLastActivity();
		chatRoomRepository.save(chatRoom);
	}

	public void cleanupInactiveRooms() {
		LocalDateTime threshold = LocalDateTime.now().minus(ROOM_INACTIVE_THRESHOLD);
		List<ChatRoom> inactiveRooms = chatRoomRepository
			.findByLastActivityBeforeAndIsActiveTrue(threshold);

		for (ChatRoom room : inactiveRooms) {
			room.setActive(false);
			chatRoomRepository.save(room);
			roomActivityCache.remove(room.getId());
		}
	}
}