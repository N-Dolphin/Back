package org.example.back.chat.config;

import org.example.back.chat.entity.ChatMessage;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.stereotype.Component;

@Component
public interface MessageProcessor {
	ProcessingResult processMessage(MessageDto messageDto);
	void handleError(ChatMessage chatMessage, Exception e);
}