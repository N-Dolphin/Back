package org.example.back.rabbitmq.service;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.repository.ChatRoomRepository;
import org.example.back.chat.service.ChatService;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {
	private final ChatService chatService;
	private final ObjectMapper objectMapper;
	private final ChatRoomRepository chatRoomRepository;

	public void receiveMessage(String message) {
		try {
			MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);
			Long fromProfileId = messageDto.fromProfileId();
			Long toProfileId = messageDto.toProfileId();

			log.info("메시지 수신: fromProfileId={}, toProfileId={}, content={}",
				fromProfileId, toProfileId, messageDto.content());

			ChatRoom chatRoom = chatRoomRepository.findByFromProfileIdAndToProfileId(
					fromProfileId, toProfileId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방이 없습니다!"));

			ChatMessage chatMessage = ChatMessage.of(
				fromProfileId, toProfileId, messageDto.content(), chatRoom);

			chatService.processReceivedMessage(chatMessage);
		} catch (JsonProcessingException e) {
			log.error("메시지 파싱 실패: {}", e.getMessage());
		}
	}
}