package org.example.back.rabbitmq.service;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.exception.ChatException;
import org.example.back.chat.repository.ChatRoomRepository;
import org.example.back.chat.service.ChatService;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {
	private final ChatService chatService;
	private final ObjectMapper objectMapper;
	private final ChatRoomRepository chatRoomRepository;
	private final SimpMessagingTemplate messagingTemplate;

	public void handleMessage(String message) {
		log.info("Received message: {}", message);
		ChatMessage chatMessage = null;
		try {
			MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);
			log.info("Parsed message DTO: {}", messageDto);

			// 채팅방 정보 조회
			Long chatRoomId = messageDto.chatRoomId();
			ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("Chat room not found!"));
			log.info("Chat room found: {}", chatRoom.getId());

			// 송수신자 ID 결정
			Long fromProfileId;
			Long toProfileId;
			if (chatRoom.getFromProfileId().equals(messageDto.fromProfileId())) {
				fromProfileId = chatRoom.getFromProfileId();
				toProfileId = chatRoom.getToProfileId();
			} else {
				fromProfileId = chatRoom.getToProfileId();
				toProfileId = chatRoom.getFromProfileId();
			}

			chatMessage = ChatMessage.of(fromProfileId, toProfileId, messageDto.content(), chatRoom);

			chatService.processReceivedMessage(chatMessage);
			log.info("Successfully processed message for room: {}", chatRoomId);

			// WebSocket 구독자들에게 메시지 전달
			messagingTemplate.convertAndSend("/api/v1/topic/chat/" + chatRoomId, chatMessage);
		} catch (Exception e) {
			log.error("Message processing failed: {}", e.getMessage(), e);
			if (chatMessage != null) {
				chatService.handleMessageError(chatMessage, e);
			} else {
				log.error("Failed to process message before ChatMessage creation: {}", e.getMessage());
				throw new ChatException("MESSAGE_PROCESSING_FAILED",
					"메시지 처리 중 오류가 발생했습니다: " + e.getMessage());
			}
		}
	}
}