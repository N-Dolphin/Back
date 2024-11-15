package org.example.back.rabbitmq.service;
import org.example.back.chat.config.ChatMessageMapper;
import org.example.back.chat.config.MessageProcessor;
import org.example.back.chat.config.ProcessingResult;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.exception.ChatException;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private final ChatMessageMapper chatMessageMapper;


	@Transactional
	public void handleMessage(String message) {
		log.info("Received message: {}", message);
		ChatMessage chatMessage = null;

		try {
			MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);
			log.info("Parsed message DTO: {}", messageDto);

			// DB 저장 및 WebSocket 브로드캐스트
			ProcessingResult result = messageProcessor.processMessage(messageDto);
			chatMessage = result.getChatMessage();

			// 저장된 메시지 ID를 포함한 DTO 생성
			MessageDto savedMessageDto = chatMessageMapper.toDto(chatMessage);

			// 구독자들에게 브로드캐스트
			messagingTemplate.convertAndSend(
				"/topic/chat/" + messageDto.chatRoomId(),
				savedMessageDto
			);

			log.info("Successfully processed and broadcast message for room: {}",
				messageDto.chatRoomId());

		} catch (Exception e) {
			log.error("Message processing failed: {}", e.getMessage(), e);
			if (chatMessage != null) {
				messageProcessor.handleError(chatMessage, e);
			} else {
				log.error("Failed to process message before ChatMessage creation: {}",
					e.getMessage());
				throw new ChatException("MESSAGE_PROCESSING_FAILED",
					"메시지 처리 중 오류가 발생했습니다: " + e.getMessage());
			}
		}
	}
}