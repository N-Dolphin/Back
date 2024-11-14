package org.example.back.chat.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.example.back.chat.dto.ChatRoomDto;
import org.example.back.chat.dto.ChatRoomInfo;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.exception.ChatException;
import org.example.back.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ChatRoomService {
	private final Cache<Long, ChatRoomInfo> roomCache;
	private static final Duration ROOM_INACTIVE_THRESHOLD = Duration.ofHours(24);
	private final ChatRoomRepository chatRoomRepository;


	public ChatRoomService(ChatRoomRepository chatRoomRepository) {
		this.chatRoomRepository = chatRoomRepository;
		this.roomCache = Caffeine.newBuilder()
			.maximumSize(10_000)  // 최대 1만개 채팅방 캐싱
			.expireAfterWrite(1, TimeUnit.HOURS)  // 1시간 후 만료
			.recordStats()  // 캐시 성능 모니터링
			.build();
	}


	public List<ChatRoomDto> getChatRooms(Long profileId) {
		return chatRoomRepository.findAllByFromProfileIdOrToProfileId(profileId, profileId)
			.stream()
			.map(room -> new ChatRoomDto(room.getId(), room.getFromProfileId(),
				room.getToProfileId(), room.getCreatedAt()))
			.toList();
	}

	public boolean isUserInChatRoom(Long profileId, Long chatRoomId) {
		ChatRoomInfo roomInfo = getChatRoomInfo(chatRoomId);
		return roomInfo != null &&
			(roomInfo.fromProfileId().equals(profileId) ||
				roomInfo.toProfileId().equals(profileId));
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
		chatRoom = chatRoomRepository.save(chatRoom);

		// 새로 생성된 채팅방 캐싱
		roomCache.put(chatRoom.getId(), ChatRoomInfo.from(chatRoom));
		log.info("채팅방 생성 완료: {}", chatRoom.getId());

		return chatRoom.getId();
	}

	@Transactional(readOnly = true)
	public ChatRoom getChatRoom(Long chatRoomId) {
		ChatRoomInfo cachedInfo = getChatRoomInfo(chatRoomId);
		if (cachedInfo != null) {
			return convertToEntity(cachedInfo);
		}

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> {
				log.error("Chat room not found: {}", chatRoomId);
				return new ChatException("CHATROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.");
			});

		// 조회된 채팅방 캐싱
		roomCache.put(chatRoomId, ChatRoomInfo.from(chatRoom));
		return chatRoom;
	}

	public ChatRoomInfo getChatRoomInfo(Long chatRoomId) {
		return roomCache.get(chatRoomId, this::loadRoomInfo);
	}

	private ChatRoomInfo loadRoomInfo(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.map(ChatRoomInfo::from)
			.orElse(null);
	}

	@Transactional
	public void updateRoomActivity(Long chatRoomId) {
		ChatRoom chatRoom = getChatRoom(chatRoomId);
		chatRoom.updateLastActivity();
		chatRoom = chatRoomRepository.save(chatRoom);

		// 캐시 업데이트
		roomCache.put(chatRoomId, ChatRoomInfo.from(chatRoom));
	}

	public void cleanupInactiveRooms() {
		LocalDateTime threshold = LocalDateTime.now().minus(ROOM_INACTIVE_THRESHOLD);
		List<ChatRoom> inactiveRooms = chatRoomRepository
			.findByLastActivityBeforeAndIsActiveTrue(threshold);

		for (ChatRoom room : inactiveRooms) {
			room.setActive(false);
			chatRoomRepository.save(room);
			roomCache.invalidate(room.getId());  // 캐시에서 제거
		}
	}

	private ChatRoom convertToEntity(ChatRoomInfo info) {
		ChatRoom chatRoom = new ChatRoom();
		chatRoom.setId(info.id());
		chatRoom.setFromProfileId(info.fromProfileId());
		chatRoom.setToProfileId(info.toProfileId());
		chatRoom.setCreatedAt(info.createdAt());
		chatRoom.setLastActivity(info.lastActivity());
		chatRoom.setActive(info.isActive());
		return chatRoom;
	}
}
