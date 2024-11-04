package org.example.back.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final ObjectMapper objectMapper;
	private final MessageValidator messageValidator;

	@Cacheable(value = "chatRooms", key = "#profileId")
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
	public void sendMessage(Long fromProfileId, Long toProfileId, String content, Long chatRoomId) {
		MessageDto messageDto = new MessageDto(fromProfileId, toProfileId, content, LocalDateTime.now());
		messageValidator.validateMessage(messageDto);

		try {
			String objectToJSON = objectMapper.writeValueAsString(messageDto);

			String exchangeName = getExchangeName(chatRoomId);
			String routingKey = getRoutingKey(chatRoomId);


			// 메시지 전송 확인을 위한 콜백 설정
			rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
				if (!ack) {
					log.error("메시지 전송 실패: {}", cause);
					// 재시도 로직 구현
				}
			});

			rabbitTemplate.convertAndSend(exchangeName, routingKey, objectToJSON);

		} catch (Exception e) {
			throw new ChatException("MESSAGE_SEND_FAILED", "메시지 전송에 실패했습니다: " + e.getMessage());
		}
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

	// 특정 채팅방의 모든 메시지 조회
	public List<ChatMessage> getMessages(Long chatRoomId) {
		return chatMessageRepository.findByChatRoomId(chatRoomId);
	}

	@Transactional
	public void processReceivedMessage(ChatMessage chatMessage) {
		ChatRoom chatRoom = chatMessage.getChatRoom();
		chatRoom.updateLastActivity(); // 메시지 수신시 lastActivity 업데이트
		chatRoomRepository.save(chatRoom);
		chatMessageRepository.save(chatMessage);
	}


	public List<MessageDto> getMessages(Long chatRoomId, int page, int size) {
		List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(chatRoomId)
			.stream()
			.skip(page * size)
			.limit(size)
			.toList();
		return messages.stream()
			.map(message -> new MessageDto(message.getSenderId(), message.getReceiverId(), message.getContent(), message.getSentAt()))
			.toList();
	}

	public boolean hasMoreMessages(Long chatRoomId, int page, int size) {
		long count = chatMessageRepository.countByChatRoomId(chatRoomId);
		return (page + 1) * size < count;
	}

	// 채팅 관련 이름 생성을 위한 유틸리티 메소드들
	private String getExchangeName(Long chatRoomId) {
		return "chat_exchange_" + chatRoomId;
	}

	private String getRoutingKey(Long chatRoomId) {
		return "chat_route_" + chatRoomId;
	}


}
