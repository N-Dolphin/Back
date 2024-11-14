package org.example.back.chat.service;

import org.example.back.chat.exception.ChatException;
import org.example.back.rabbitmq.MessageDto;
import org.example.back.rabbitmq.service.DynamicQueueService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageBrokerService {
	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;
	private final DynamicQueueService dynamicQueueService;

	public void publishMessage(Long chatRoomId, MessageDto messageDto) {
		try {
			String message = objectMapper.writeValueAsString(messageDto);
			rabbitTemplate.convertAndSend(
				getExchangeName(chatRoomId),
				getRoutingKey(chatRoomId),
				message
			);
			log.info("Message published to RabbitMQ for room: {}", chatRoomId);
		} catch (Exception e) {
			log.error("Failed to publish message to RabbitMQ", e);
			throw new ChatException("MESSAGE_PUBLISH_FAILED",
				"메시지 발행에 실패했습니다: " + e.getMessage());
		}
	}

	public void setupMessageQueue(Long chatRoomId) {
		dynamicQueueService.createQueueAndListener(chatRoomId);
	}

	public void removeMessageQueue(Long chatRoomId) {
		dynamicQueueService.removeQueueAndListener(chatRoomId);
	}

	private String getExchangeName(Long chatRoomId) {
		return "chat_exchange_" + chatRoomId;
	}

	private String getRoutingKey(Long chatRoomId) {
		return "chat_route_" + chatRoomId;
	}
}