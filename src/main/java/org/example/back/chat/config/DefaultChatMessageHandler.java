package org.example.back.chat.config;

import org.example.back.rabbitmq.service.ConsumerService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@Primary
@RequiredArgsConstructor
public class DefaultChatMessageHandler implements ChatMessageHandler {
	private final ConsumerService consumerService;

	@Override
	public void handleMessage(String message) {
		consumerService.handleMessage(message);
	}
}
