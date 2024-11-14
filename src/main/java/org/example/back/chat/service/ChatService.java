package org.example.back.chat.service;

import static org.example.back.chat.entity.ChatMessage.MessageStatus.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.example.back.chat.config.ChatMessageMapper;
import org.example.back.chat.config.MessageValidator;
import org.example.back.chat.dto.ChatRoomDto;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.exception.ChatException;
import org.example.back.chat.repository.ChatMessageRepository;
import org.example.back.chat.repository.ChatRoomRepository;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;
	private final MessageValidator messageValidator;
	private final ChatMessageMapper chatMessageMapper;



	// 채팅방 활성화 상태를 추적하기 위한 캐시
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
			(chatRoom.get().getFromProfileId().equals(profileId) || chatRoom.get().getToProfileId().equals(profileId));
	}


	@Transactional
	public Long sendMessage(Long fromProfileId, Long toProfileId, String content, Long chatRoomId) {
		log.info("Processing message - from: {}, to: {}, room: {}",
			fromProfileId, toProfileId, chatRoomId);

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> {
				log.error("Chat room not found: {}", chatRoomId);
				return new ChatException("CHATROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.");
			});

		ChatMessage chatMessage = ChatMessage.of(fromProfileId, toProfileId, content, chatRoom);
		chatMessage = chatMessageRepository.save(chatMessage);
		log.info("Chat message saved to DB with ID: {}", chatMessage.getId());

		try {
			MessageDto messageDto = chatMessageMapper.toDto(chatMessage);
			String message = objectMapper.writeValueAsString(messageDto);
			rabbitTemplate.convertAndSend(getExchangeName(chatRoomId),
				getRoutingKey(chatRoomId), message);

			updateRoomActivity(chatRoomId);
			return chatMessage.getId();
		} catch (Exception e) {
			log.error("Failed to send message to RabbitMQ", e);
			chatMessage.setStatus(ChatMessage.MessageStatus.FAILED);
			chatMessageRepository.save(chatMessage);
			throw new ChatException("MESSAGE_SEND_FAILED",
				"메시지 전송에 실패했습니다: " + e.getMessage());
		}
	}

	// 메시지 유효성 검사
	public void validateMessage(MessageDto messageDto) {
		if (messageDto.fromProfileId() == null || messageDto.toProfileId() == null) {
			throw new ChatException("INVALID_MESSAGE", "송수신자 정보가 누락되었습니다.");
		}
		if (StringUtils.isEmpty(messageDto.content())) {
			throw new ChatException("INVALID_MESSAGE", "메시지 내용이 비어있습니다.");
		}
	}


	// 메시지 에러 처리
	public void handleMessageError(ChatMessage chatMessage, Exception e) {
		log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
		chatMessage.setStatus(FAILED);
		chatMessageRepository.save(chatMessage);
		throw new ChatException("MESSAGE_SEND_FAILED", "메시지 전송에 실패했습니다: " + e.getMessage());
	}

	// 채팅방 활동 시간 업데이트
	private void updateRoomActivity(Long chatRoomId) {
		LocalDateTime now = LocalDateTime.now();
		roomActivityCache.put(chatRoomId, now);

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new ChatException("CHATROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."));
		chatRoom.updateLastActivity();
		chatRoomRepository.save(chatRoom);
	}



	@Transactional
	public Long createChatRoom(Long fromProfileId, Long toProfileId) {
		Optional<ChatRoom> existingRoom = chatRoomRepository.findByProfiles(fromProfileId, toProfileId);
		ChatRoom chatRoom = new ChatRoom();

		if (!existingRoom.isPresent()) {
			// 채팅방 생성
			chatRoom.setFromProfileId(fromProfileId);
			chatRoom.setToProfileId(toProfileId);
			chatRoomRepository.save(chatRoom);
			System.out.println("채팅방 생성 완료: " + chatRoom.getId());

			return chatRoom.getId();
		} else {
			System.out.println("이미 존재하는 채팅방입니다: ");
			return existingRoom.get().getId();
		}

	}


	@Transactional
	public void processReceivedMessage(ChatMessage chatMessage) {
		ChatRoom chatRoom = chatMessage.getChatRoom();
		chatRoom.updateLastActivity();
		chatRoomRepository.save(chatRoom);
		chatMessageRepository.save(chatMessage);
		updateRoomActivity(chatRoom.getId());
	}
	public List<MessageDto> getMessages(Long chatRoomId, int page, int size) {
		List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(chatRoomId)
			.stream()
			.skip(page * size)
			.limit(size)
			.toList();
		return messages.stream()
			.map(chatMessageMapper::toDto)  // 매퍼 사용
			.toList();
	}

	@Transactional(readOnly = true)
	public ChatRoom getChatRoom(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> {
				log.error("Chat room not found: {}", chatRoomId);
				return new ChatException("CHATROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.");
			});
	}

	public boolean hasMoreMessages(Long chatRoomId, int page, int size) {
		long count = chatMessageRepository.countByChatRoomId(chatRoomId);
		return (page + 1) * size < count;
	}

	// 채팅 관련 이름 생성을 위한 유틸리티 메소드들
	public String getExchangeName(Long chatRoomId) {
		return "chat_exchange_" + chatRoomId;
	}

	public String getRoutingKey(Long chatRoomId) {
		return "chat_route_" + chatRoomId;
	}


}
