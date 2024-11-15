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
	private static final String CHAT_EXCHANGE = "chat.direct.exchange";
	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	public void publishMessage(Long chatRoomId, MessageDto messageDto) {
		try {
			String message = objectMapper.writeValueAsString(messageDto);
			String routingKey = String.format("chat.room.%d", chatRoomId);

			rabbitTemplate.convertAndSend(CHAT_EXCHANGE, routingKey, message);
			log.info("Message published to exchange: {} with routing key: {}",
				CHAT_EXCHANGE, routingKey);

		} catch (Exception e) {
			log.error("Failed to publish message", e);
			throw new ChatException("MESSAGE_PUBLISH_FAILED", e.getMessage());
		}
	}
}