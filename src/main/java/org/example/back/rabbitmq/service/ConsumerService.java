package org.example.back.rabbitmq.service;
import org.example.back.chat.config.MessageProcessor;
import org.example.back.chat.config.ProcessingResult;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.exception.ChatException;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {
	private final ObjectMapper objectMapper;
	private final MessageProcessor messageProcessor;
	private final SimpMessagingTemplate messagingTemplate;

	public void handleMessage(String message) {
		log.info("Received message: {}", message);
		ChatMessage chatMessage = null;

		try {
			MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);
			log.info("Parsed message DTO: {}", messageDto);

			// MessageProcessor로 메시지 처리 및 ChatMessage 반환
			ProcessingResult result = messageProcessor.processMessage(messageDto);
			chatMessage = result.getChatMessage();

			log.info("Successfully processed message for room: {}", messageDto.chatRoomId());

			// WebSocket 구독자들에게 메시지 전달 - 실제 ChatMessage 객체 전송
			messagingTemplate.convertAndSend("/api/v1/topic/chat/" + messageDto.chatRoomId(),
				chatMessage);

		} catch (Exception e) {
			log.error("Message processing failed: {}", e.getMessage(), e);
			if (chatMessage != null) {
				messageProcessor.handleError(chatMessage, e);
			} else {
				log.error("Failed to process message before ChatMessage creation: {}", e.getMessage());
				throw new ChatException("MESSAGE_PROCESSING_FAILED",
					"메시지 처리 중 오류가 발생했습니다: " + e.getMessage());
			}
		}
	}
}